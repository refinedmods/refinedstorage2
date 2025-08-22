package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskImpl implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskImpl.class);

    private final TaskId id;
    private final ResourceKey resource;
    private final long amount;
    private final Actor actor;
    private final boolean notify;
    private final long startTime;
    private final Map<Pattern, AbstractTaskPattern> patterns;
    private final List<AbstractTaskPattern> completedPatterns = new ArrayList<>();
    private final MutableResourceList initialRequirements;
    private final MutableResourceList internalStorage;
    private TaskState state = TaskState.READY;
    private boolean cancelled;

    public TaskImpl(final TaskSnapshot snapshot) {
        this.id = snapshot.id();
        this.resource = snapshot.resource();
        this.amount = snapshot.amount();
        this.actor = snapshot.actor();
        this.notify = snapshot.notifyActor();
        this.startTime = snapshot.startTime();
        this.patterns = snapshot.patterns().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().toTaskPattern(),
            (a, b) -> a,
            LinkedHashMap::new
        ));
        snapshot.completedPatterns().forEach(patternSnapshot -> completedPatterns.add(patternSnapshot.toTaskPattern()));
        this.initialRequirements = snapshot.copyInitialRequirements();
        this.internalStorage = snapshot.copyInternalStorage();
        this.state = snapshot.state();
        this.cancelled = snapshot.cancelled();
    }

    public TaskImpl(final TaskPlan plan, final Actor actor, final boolean notify) {
        this(plan, MutableResourceListImpl.create(), actor, notify);
    }

    TaskImpl(final TaskPlan plan, final MutableResourceList internalStorage, final Actor actor, final boolean notify) {
        this.id = TaskId.create();
        this.internalStorage = internalStorage;
        this.resource = plan.resource();
        this.amount = plan.amount();
        this.actor = actor;
        this.notify = notify;
        this.startTime = System.currentTimeMillis();
        this.patterns = plan.patterns().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> createTaskPattern(e.getKey(), e.getValue()),
            (a, b) -> a,
            LinkedHashMap::new
        ));
        this.initialRequirements = MutableResourceListImpl.create();
        plan.initialRequirements().forEach(initialRequirements::add);
    }

    private static AbstractTaskPattern createTaskPattern(final Pattern pattern,
                                                         final TaskPlan.PatternPlan patternPlan) {
        return switch (pattern.layout().type()) {
            case INTERNAL -> new InternalTaskPattern(pattern, patternPlan);
            case EXTERNAL -> new ExternalTaskPattern(pattern, patternPlan);
        };
    }

    @Override
    public Actor getActor() {
        return actor;
    }

    @Override
    public boolean shouldNotify() {
        return notify && !cancelled;
    }

    @Override
    public ResourceKey getResource() {
        return resource;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public TaskId getId() {
        return id;
    }

    @Override
    public TaskState getState() {
        return state;
    }

    private void updateState(final TaskState newState) {
        LOGGER.debug("Task {} state changed from {} to {}", id.id(), state, newState);
        this.state = newState;
    }

    @Override
    public boolean step(final RootStorage rootStorage,
                        final ExternalPatternSinkProvider sinkProvider,
                        final StepBehavior stepBehavior,
                        final TaskListener listener) {
        return switch (state) {
            case READY -> startTask(rootStorage);
            case EXTRACTING_INITIAL_RESOURCES -> extractInitialResourcesAndTryStartRunningTask(rootStorage);
            case RUNNING -> stepPatterns(rootStorage, sinkProvider, stepBehavior, listener);
            case RETURNING_INTERNAL_STORAGE -> returnInternalStorageAndTryCompleteTask(rootStorage);
            case COMPLETED -> false;
        };
    }

    @Override
    public void cancel() {
        state = TaskState.RETURNING_INTERNAL_STORAGE;
        cancelled = true;
    }

    @Override
    public TaskStatus getStatus() {
        final TaskStatusBuilder builder = new TaskStatusBuilder(id, state, resource, amount, startTime);
        initialRequirements.getAll().forEach(
            requiredResource -> builder.extracting(requiredResource, initialRequirements.get(requiredResource))
        );
        double totalWeightedCompleted = 0;
        double totalWeight = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            pattern.appendStatus(builder);
            totalWeightedCompleted += pattern.getPercentageCompleted() * pattern.getWeight();
            totalWeight += pattern.getWeight();
        }
        for (final AbstractTaskPattern pattern : completedPatterns) {
            totalWeightedCompleted += pattern.getWeight();
            totalWeight += pattern.getWeight();
        }
        internalStorage.getAll().forEach(
            internalResource -> builder.stored(internalResource, internalStorage.get(internalResource))
        );
        return builder.build(totalWeight == 0 ? 0 : totalWeightedCompleted / totalWeight);
    }

    public TaskSnapshot createSnapshot() {
        return new TaskSnapshot(
            id,
            resource,
            amount,
            actor,
            notify,
            startTime,
            patterns.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().createSnapshot(),
                (a, b) -> a,
                LinkedHashMap::new
            )),
            completedPatterns.stream()
                .filter(InternalTaskPattern.class::isInstance)
                .map(InternalTaskPattern.class::cast)
                .map(InternalTaskPattern::createSnapshot)
                .toList(),
            initialRequirements.copy(),
            internalStorage.copy(),
            state,
            cancelled
        );
    }

    private boolean startTask(final RootStorage rootStorage) {
        updateState(TaskState.EXTRACTING_INITIAL_RESOURCES);
        return extractInitialResourcesAndTryStartRunningTask(rootStorage);
    }

    private boolean extractInitialResourcesAndTryStartRunningTask(final RootStorage rootStorage) {
        boolean extractedAll = true;
        boolean extractedAny = false;
        final Set<ResourceKey> initialRequirementResources = new HashSet<>(initialRequirements.getAll());
        for (final ResourceKey initialRequirementResource : initialRequirementResources) {
            final long needed = initialRequirements.get(initialRequirementResource);
            final long extracted = rootStorage.extract(initialRequirementResource, needed, Action.EXECUTE, Actor.EMPTY);
            if (extracted > 0) {
                extractedAny = true;
            }
            LOGGER.debug("Extracted {}x {} from storage", extracted, initialRequirementResource);
            if (extracted != needed) {
                extractedAll = false;
            }
            if (extracted > 0) {
                initialRequirements.remove(initialRequirementResource, extracted);
                internalStorage.add(initialRequirementResource, extracted);
            }
        }
        if (extractedAll) {
            updateState(TaskState.RUNNING);
        }
        return extractedAny;
    }

    private boolean stepPatterns(final RootStorage rootStorage,
                                 final ExternalPatternSinkProvider sinkProvider,
                                 final StepBehavior stepBehavior,
                                 final TaskListener listener) {
        final var it = patterns.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            final var pattern = it.next();
            final PatternStepResult result = stepPattern(rootStorage, sinkProvider, stepBehavior, listener, pattern);
            if (result == PatternStepResult.COMPLETED) {
                it.remove();
            }
            changed |= result.isChanged();
        }
        if (patterns.isEmpty()) {
            if (internalStorage.isEmpty()) {
                updateState(TaskState.COMPLETED);
            } else {
                updateState(TaskState.RETURNING_INTERNAL_STORAGE);
            }
        }
        return changed;
    }

    private PatternStepResult stepPattern(final RootStorage rootStorage,
                                          final ExternalPatternSinkProvider sinkProvider,
                                          final StepBehavior stepBehavior,
                                          final TaskListener listener,
                                          final Map.Entry<Pattern, AbstractTaskPattern> pattern) {
        PatternStepResult result = PatternStepResult.IDLE;
        if (!stepBehavior.canStep(pattern.getKey())) {
            return result;
        }
        final int steps = stepBehavior.getSteps(pattern.getKey());
        for (int i = 0; i < steps; ++i) {
            final PatternStepResult stepResult = pattern.getValue().step(
                internalStorage,
                rootStorage,
                sinkProvider,
                listener
            );
            if (stepResult == PatternStepResult.COMPLETED) {
                LOGGER.debug("{} completed", pattern.getKey());
                completedPatterns.add(pattern.getValue());
                return stepResult;
            } else if (stepResult != PatternStepResult.IDLE) {
                result = PatternStepResult.RUNNING;
            }
        }
        return result;
    }

    private boolean returnInternalStorageAndTryCompleteTask(final RootStorage rootStorage) {
        boolean returnedAll = true;
        boolean returnedAny = false;
        final Set<ResourceKey> internalResources = new HashSet<>(internalStorage.getAll());
        for (final ResourceKey internalResource : internalResources) {
            final long internalAmount = internalStorage.get(internalResource);
            final long inserted = rootStorage.insert(internalResource, internalAmount, Action.EXECUTE, Actor.EMPTY);
            if (inserted > 0) {
                returnedAny = true;
            }
            LOGGER.debug("Returned {}x {} into storage", inserted, internalResource);
            if (inserted != internalAmount) {
                returnedAll = false;
            }
            if (inserted > 0) {
                internalStorage.remove(internalResource, inserted);
            }
        }
        if (returnedAll) {
            updateState(TaskState.COMPLETED);
        }
        return returnedAny;
    }

    @Override
    public long beforeInsert(final ResourceKey insertedResource, final long insertedAmount) {
        long intercepted = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            final long available = insertedAmount - intercepted;
            intercepted += pattern.beforeInsert(insertedResource, available);
            if (intercepted == insertedAmount) {
                internalStorage.add(insertedResource, intercepted);
                return intercepted;
            }
        }
        if (intercepted > 0) {
            internalStorage.add(insertedResource, intercepted);
        }
        return intercepted;
    }

    @Override
    public long afterInsert(final ResourceKey insertedResource, final long insertedAmount) {
        long reserved = 0;
        for (final AbstractTaskPattern pattern : patterns.values()) {
            final long available = insertedAmount - reserved;
            reserved += pattern.afterInsert(insertedResource, available);
            if (reserved == insertedAmount) {
                return reserved;
            }
        }
        return reserved;
    }

    @Override
    public void changed(final MutableResourceList.OperationResult change) {
        // no op
    }
}
