package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpCraftingSolver;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskSnapshot;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpDispatcherHelper;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPlanningHelper;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpStepPlan;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpStepPlanCalculator;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.api.autocrafting.craftability.IsCraftableCraftingCalculatorListener.binarySearchMaxAmount;
import static com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewCraftingCalculatorListener.calculateTree;
import static com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlanCraftingCalculatorListener.calculatePlan;

public class AutocraftingNetworkComponentImpl implements AutocraftingNetworkComponent, ParentContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutocraftingNetworkComponentImpl.class);

    private final Supplier<RootStorage> rootStorageProvider;
    private final ExecutorService executorService;
    private final Set<PatternProvider> providers = new HashSet<>();
    private final Map<Pattern, PatternProvider> providerByPattern = new HashMap<>();
    private final Map<PatternLayout, List<ExternalPatternSink>> sinksByPatternLayout = new HashMap<>();
    private final Map<TaskId, PatternProvider> providerByTaskId = new HashMap<>();
    private final Set<PatternListener> patternListeners = new HashSet<>();
    private final Set<TaskStatusListener> statusListeners = new HashSet<>();
    private final PatternRepositoryImpl patternRepository = new PatternRepositoryImpl();

    public AutocraftingNetworkComponentImpl(final Supplier<RootStorage> rootStorageProvider,
                                            final ExecutorService executorService) {
        this.rootStorageProvider = rootStorageProvider;
        this.executorService = executorService;
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof PatternProvider provider) {
            provider.onAddedIntoContainer(this);
            providers.add(provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof PatternProvider provider) {
            provider.onRemovedFromContainer(this);
            providers.remove(provider);
        }
    }

    @Override
    public Set<ResourceKey> getOutputs() {
        return patternRepository.getOutputs();
    }

    @Override
    public boolean contains(final AutocraftingNetworkComponent component) {
        return providers.stream().anyMatch(provider -> provider.contains(component));
    }

    @Nullable
    @Override
    public PatternProvider getProviderByPattern(final Pattern pattern) {
        return providerByPattern.get(pattern);
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount,
                                                           final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        try {
            return CompletableFuture.supplyAsync(() -> {
                final RootStorage rootStorage = rootStorageProvider.get();
                final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
                final Preview preview = PreviewCraftingCalculatorListener.calculatePreview(calculator, resource, amount,
                    cancellationToken);
                return Optional.of(preview);
            }, executorService);
        } catch (final RejectedExecutionException e) {
            return CompletableFuture.completedFuture(Optional.of(new Preview(
                PreviewType.NOT_AVAILABLE, Collections.emptyList(), Collections.emptyList())));
        }
    }

    @Override
    public CompletableFuture<Optional<TreePreview>> getTreePreview(final ResourceKey resource, final long amount,
                                                                   final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        try {
            return CompletableFuture.supplyAsync(() -> {
                final RootStorage rootStorage = rootStorageProvider.get();
                final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
                final TreePreview tree = calculateTree(calculator, resource, amount, cancellationToken);
                return Optional.of(tree);
            }, executorService);
        } catch (final RejectedExecutionException e) {
            return CompletableFuture.completedFuture(Optional.of(new TreePreview(
                PreviewType.NOT_AVAILABLE, null, Collections.emptyList())));
        }
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource,
                                                final CancellationToken cancellationToken) {
        CoreValidations.validateNotNull(resource, "Resource cannot be null");
        final RootStorage rootStorage = rootStorageProvider.get();
        final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
        return CompletableFuture.supplyAsync(
            () -> binarySearchMaxAmount(calculator, resource, cancellationToken),
            executorService
        );
    }

    @Override
    public Optional<TaskId> startTask(final ResourceKey resource,
                                  final long amount,
                                  final Actor actor,
                                  final boolean notify,
                                  final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        final RootStorage rootStorage = rootStorageProvider.get();
        final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);

        // Determine which system to use
        if (shouldUseLPSystem(resource, rootStorage)) {
            final Collection<Pattern> relevantPatterns = collectRelevantPatternsForLp(resource, rootStorage);
            return LpStepPlanCalculator.calculateSteps(
                relevantPatterns,
                LOGGER,
                rootStorage,
                resource,
                amount,
                cancellationToken
            )
                .flatMap(steps -> addLpDispatcherTask(resource, amount, actor, steps, notify));
        } else {
            return calculatePlan(calculator, resource, amount, cancellationToken)
                .map(plan -> addTask(resource, amount, actor, plan, notify));
        }
    }

    @Override
    public EnsureResult ensureTask(final ResourceKey resource, final long amount, final Actor actor,
                               final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        final long currentlyCrafting = providers.stream()
            .mapToLong(provider -> provider.getAmount(resource))
            .sum();
        if (currentlyCrafting >= amount) {
            return EnsureResult.TASK_ALREADY_RUNNING;
        }
        final RootStorage rootStorage = rootStorageProvider.get();
        final long correctedAmount = amount - currentlyCrafting;
        final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);

        // Determine which system to use
        if (shouldUseLPSystem(resource, rootStorage)) {
            final Collection<Pattern> relevantPatterns = collectRelevantPatternsForLp(resource, rootStorage);
            return LpStepPlanCalculator.calculateSteps(
                relevantPatterns,
                LOGGER,
                rootStorage,
                resource,
                correctedAmount,
                cancellationToken
            )
                .flatMap(steps -> addLpDispatcherTask(resource, correctedAmount, actor, steps, false))
                .map(taskId -> EnsureResult.TASK_CREATED)
                .orElseGet(() -> ensureTaskForCraftableAmountViaLp(
                    relevantPatterns,
                    rootStorage,
                    resource,
                    correctedAmount,
                    actor,
                    cancellationToken
                ));
        } else {
            return calculatePlan(calculator, resource, correctedAmount, cancellationToken)
                .map(plan -> addTask(resource, correctedAmount, actor, plan, false))
                .map(taskId -> EnsureResult.TASK_CREATED)
                .orElseGet(() -> ensureTaskForCraftableAmount(resource, actor, correctedAmount, calculator,
                    cancellationToken));
        }
    }

    private boolean shouldUseLPSystem(final ResourceKey requestedResource) {
        return LpPlanningHelper.shouldUseLPSystem(requestedResource, rootStorageProvider.get(), patternRepository);
    }

    private boolean shouldUseLPSystem(final ResourceKey requestedResource, final RootStorage rootStorage) {
        return LpPlanningHelper.shouldUseLPSystem(requestedResource, rootStorage, patternRepository);
    }

    private EnsureResult ensureTaskForCraftableAmount(final ResourceKey resource, final Actor actor,
                                                      final long amount, final CraftingCalculator calculator,
                                                      final CancellationToken cancellationToken) {
        final long correctedAmount = Math.min(
            binarySearchMaxAmount(calculator, resource, CancellationToken.NONE),
            amount
        );
        if (correctedAmount <= 0) {
            return EnsureResult.MISSING_RESOURCES;
        }
        return calculatePlan(calculator, resource, correctedAmount, cancellationToken)
            .map(plan -> addTask(resource, correctedAmount, actor, plan, false))
            .map(taskId -> EnsureResult.TASK_CREATED)
            .orElse(EnsureResult.MISSING_RESOURCES);
    }
    private Collection<Pattern> collectRelevantPatternsForLp(final ResourceKey requestedResource,
                                                             final RootStorage rootStorage) {
        return LpPlanningHelper.collectRelevantPatternsForLp(requestedResource, rootStorage, patternRepository);
    }

    private EnsureResult ensureTaskForCraftableAmountViaLp(final Collection<Pattern> relevantPatterns,
                                                           final RootStorage rootStorage,
                                                           final ResourceKey resource,
                                                           final long amount,
                                                           final Actor actor,
                                                           final CancellationToken cancellationToken) {
        if (cancellationToken.isCancelled()) {
            return EnsureResult.MISSING_RESOURCES;
        }

        final long correctedAmount = findMaxCraftableAmountViaLp(
            relevantPatterns,
            rootStorage,
            resource,
            amount,
            cancellationToken
        );
        if (correctedAmount <= 0) {
            return EnsureResult.MISSING_RESOURCES;
        }

        return LpStepPlanCalculator.calculateSteps(
            patternRepository.getAll(),
            LOGGER,
            rootStorage,
            resource,
            correctedAmount,
            cancellationToken
        )
            .flatMap(steps -> addLpDispatcherTask(resource, correctedAmount, actor, steps, false))
            .map(taskId -> EnsureResult.TASK_CREATED)
            .orElse(EnsureResult.MISSING_RESOURCES);
    }

    private long findMaxCraftableAmountViaLp(final Collection<Pattern> relevantPatterns,
                                             final RootStorage rootStorage,
                                             final ResourceKey resource,
                                             final long amount,
                                             final CancellationToken cancellationToken) {
        long low = 1;
        long high = amount;
        long best = 0;

        while (low <= high && !cancellationToken.isCancelled()) {
            final long middle = low + ((high - low) / 2);
            final boolean craftable = LpStepPlanCalculator.calculateSteps(
                relevantPatterns,
                LOGGER,
                rootStorage,
                resource,
                middle,
                cancellationToken
            ).isPresent();
            if (craftable) {
                best = middle;
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }

        return best;
    }

    private Optional<TaskId> addLpDispatcherTask(final ResourceKey resource,
                                                 final long amount,
                                                 final Actor actor,
                                                 final LpStepPlan lpStepPlan,
                                                 final boolean notify) {
        if (lpStepPlan.steps().size() == 1) {
            final TaskPlan singleStepPlan = toSingleStepPlan(resource, amount, lpStepPlan.steps().getFirst(), true);
            return Optional.of(addSingleStepTask(actor, singleStepPlan, notify));
        }

        final Pattern rootPattern = LpDispatcherHelper.findRootPattern(resource, lpStepPlan.steps());
        if (rootPattern == null) {
            return Optional.empty();
        }

        final PatternProvider provider = providerByPattern.get(rootPattern);
        if (provider == null) {
            return Optional.empty();
        }

        final Task dispatcher = new LpStepDispatcher(resource, amount, actor, notify, lpStepPlan, rootPattern);
        provider.addTask(dispatcher);
        return Optional.of(dispatcher.getId());
    }

    private Optional<TaskPlan> toTaskPlan(final ResourceKey resource,
                                          final long amount,
                                          final LpCraftingSolver.PlanningOutcome outcome) {
        if (outcome.executableResult().isEmpty()) {
            return Optional.empty();
        }
        final List<LpExecutionPlanStep> steps = outcome.executableResult().get().plan();
        return LpDispatcherHelper.toTaskPlan(resource, amount, steps);
    }

    private TaskId addTask(final ResourceKey resource,
                           final long amount,
                           final Actor actor,
                           final TaskPlan plan,
                           final boolean notify) {
        final Task task = new TaskImpl(plan, actor, notify);
        LOGGER.debug("Created task {} for {}x {} for {}", task.getId(), amount, resource, actor);
        final PatternProvider provider = CoreValidations.validateNotNull(
            providerByPattern.get(plan.rootPattern()),
            "No provider for pattern " + plan.rootPattern()
        );
        provider.addTask(task);
        return task.getId();
    }

    private TaskId addSingleStepTask(final Actor actor,
                                     final TaskPlan plan,
                                     final boolean notify) {
        final Task task = new TaskImpl(plan, actor, notify);
        LOGGER.debug(
            "Created single-step LP task {} for {}x {} for {}",
            task.getId(),
            plan.amount(),
            plan.resource(),
            actor
        );

        final PatternProvider provider;
        if (plan.rootPattern().layout().type() == PatternType.EXTERNAL) {
            provider = getSinksByPatternLayout(plan.rootPattern().layout()).stream()
                .filter(PatternProvider.class::isInstance)
                .map(PatternProvider.class::cast)
                .findFirst()
                .orElseGet(() -> CoreValidations.validateNotNull(
                    providerByPattern.get(plan.rootPattern()),
                    "No provider for pattern " + plan.rootPattern()
                ));
        } else {
            provider = CoreValidations.validateNotNull(
                providerByPattern.get(plan.rootPattern()),
                "No provider for pattern " + plan.rootPattern()
            );
        }

        provider.addTask(task);
        return task.getId();
    }

    @Override
    public void addListener(final PatternListener listener) {
        patternListeners.add(listener);
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        statusListeners.add(listener);
    }

    @Override
    public void removeListener(final PatternListener listener) {
        patternListeners.remove(listener);
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        statusListeners.remove(listener);
    }

    @Override
    public Set<Pattern> getPatterns() {
        return patternRepository.getAll();
    }

    @Override
    public List<Pattern> getPatternsByOutput(final ResourceKey output) {
        return patternRepository.getByOutput(output);
    }

    @Override
    public List<TaskStatus> getStatuses() {
        return providers.stream().map(PatternProvider::getTaskStatuses).flatMap(List::stream).toList();
    }

    @Override
    public void cancel(final TaskId taskId) {
        final PatternProvider provider = providerByTaskId.get(taskId);
        if (provider == null) {
            return;
        }
        provider.cancelTask(taskId);
    }

    @Override
    public void cancelAll() {
        for (final Map.Entry<TaskId, PatternProvider> entry : providerByTaskId.entrySet()) {
            final PatternProvider provider = entry.getValue();
            final TaskId taskId = entry.getKey();
            provider.cancelTask(taskId);
        }
    }

    @Override
    public void add(final PatternProvider provider, final Pattern pattern, final int priority) {
        patternRepository.add(pattern, priority);
        providerByPattern.put(pattern, provider);
        final List<ExternalPatternSink> sinks = sinksByPatternLayout.computeIfAbsent(
            pattern.layout(),
            layout -> new ArrayList<>()
        );
        if (!sinks.contains(provider)) {
            sinks.add(provider);
        }
        patternListeners.forEach(listener -> listener.onAdded(pattern));
    }

    @Override
    public void remove(final PatternProvider provider, final Pattern pattern) {
        patternListeners.forEach(listener -> listener.onRemoved(pattern));
        providerByPattern.remove(pattern);
        final List<ExternalPatternSink> sinksByLayout = sinksByPatternLayout.get(pattern.layout());
        if (sinksByLayout != null) {
            sinksByLayout.remove(provider);
            if (sinksByLayout.isEmpty()) {
                sinksByPatternLayout.remove(pattern.layout());
            }
        }
        patternRepository.remove(pattern);
    }

    @Override
    public void update(final Pattern pattern, final int priority) {
        patternRepository.update(pattern, priority);
    }

    @Override
    public void taskAdded(final PatternProvider provider, final Task task) {
        providerByTaskId.put(task.getId(), provider);
        statusListeners.forEach(listener -> listener.taskAdded(task.getStatus()));
    }

    @Override
    public void taskRemoved(final Task task) {
        providerByTaskId.remove(task.getId());
        statusListeners.forEach(listener -> listener.taskRemoved(task.getId()));
    }

    @Override
    public void taskCompleted(final Task task) {
        taskRemoved(task);
    }

    @Override
    public void taskChanged(final Task task) {
        if (statusListeners.isEmpty()) {
            return;
        }
        final TaskStatus status = task.getStatus();
        statusListeners.forEach(listener -> listener.taskStatusChanged(status));
    }

    @Override
    public List<ExternalPatternSink> getSinksByPatternLayout(final PatternLayout patternLayout) {
        return sinksByPatternLayout.getOrDefault(patternLayout, Collections.emptyList());
    }

    private TaskPlan toSingleStepPlan(final ResourceKey requestedResource,
                                      final long requestedAmount,
                                      final LpExecutionPlanStep step,
                                      final boolean root) {
        return LpDispatcherHelper.toSingleStepPlan(requestedResource, requestedAmount, step, root);
    }

    private static TaskPlan createDispatcherPlan(final ResourceKey resource,
                                                 final long amount,
                                                 final Pattern rootPattern) {
        return LpDispatcherHelper.createDispatcherPlan(resource, amount, rootPattern);
    }

    private final class LpStepDispatcher extends TaskImpl {
        private final long startTime = System.currentTimeMillis();
        private final int totalSteps;
        private final List<LpExecutionPlanStep> pendingSteps = new ArrayList<>();
        private final MutableResourceList bufferedInternalStorage = MutableResourceListImpl.create();
        private final Map<TaskId, DispatchedSubTask> activeSubTasks = new LinkedHashMap<>();
        private final boolean strictOrdering;
        private TaskState state = TaskState.READY;
        private boolean cancelled;

        private LpStepDispatcher(final ResourceKey resource,
                                 final long amount,
                                 final Actor actor,
                                 final boolean notify,
                                 final LpStepPlan lpStepPlan,
                                 final Pattern rootPattern) {
            super(createDispatcherPlan(resource, amount, rootPattern), actor, notify);
            this.strictOrdering = lpStepPlan.hasRecipeCycles();
            this.pendingSteps.addAll(lpStepPlan.steps());
            this.totalSteps = lpStepPlan.steps().size();
        }

        @Override
        public boolean shouldNotify() {
            return super.shouldNotify() && !cancelled;
        }

        @Override
        public TaskState getState() {
            return state;
        }

        @Override
        public boolean step(final RootStorage rootStorage,
                            final ExternalPatternSinkProvider sinkProvider,
                            final StepBehavior stepBehavior,
                            final TaskListener listener) {
            boolean changed = pruneCompletedSubTasks();

            if (state == TaskState.READY) {
                state = TaskState.RUNNING;
                changed = true;
            }

            if (cancelled) {
                cancelActiveSubTasks();
                if (activeSubTasks.isEmpty()) {
                    state = TaskState.COMPLETED;
                    return true;
                }
                state = TaskState.RETURNING_INTERNAL_STORAGE;
                return changed;
            }

            if (state == TaskState.RUNNING) {
                final Set<TaskId> existingSubTasks = new HashSet<>(activeSubTasks.keySet());
                changed |= stepSpecificSubTasks(existingSubTasks, rootStorage, sinkProvider, stepBehavior, listener);
                changed |= pruneCompletedSubTasks();

                if (strictOrdering) {
                    changed |= dispatchStrict(rootStorage);
                } else {
                    changed |= dispatchRelaxed(rootStorage);
                }

                final Set<TaskId> newSubTasks = new HashSet<>(activeSubTasks.keySet());
                newSubTasks.removeAll(existingSubTasks);
                changed |= stepSpecificSubTasks(newSubTasks, rootStorage, sinkProvider, stepBehavior, listener);
                changed |= pruneCompletedSubTasks();

                if (pendingSteps.isEmpty() && activeSubTasks.isEmpty()) {
                    state = TaskState.COMPLETED;
                    changed = true;
                }
            }

            return changed;
        }

        private boolean pruneCompletedSubTasks() {
            boolean changed = false;
            final var it = activeSubTasks.entrySet().iterator();
            while (it.hasNext()) {
                final var entry = it.next();
                final TaskState subTaskState = entry.getValue().task().getState();
                if (subTaskState == TaskState.COMPLETED
                    || (!entry.getValue().root() && subTaskState == TaskState.RETURNING_INTERNAL_STORAGE)) {
                    final MutableResourceList subTaskInternalStorage = entry.getValue()
                        .task()
                        .createSnapshot()
                        .copyInternalStorage();
                    subTaskInternalStorage.getAll().forEach(resource ->
                        bufferedInternalStorage.add(resource, subTaskInternalStorage.get(resource)));
                    it.remove();
                    changed = true;
                }
            }
            return changed;
        }

        private void cancelActiveSubTasks() {
            final List<DispatchedSubTask> snapshot = List.copyOf(activeSubTasks.values());
            for (final DispatchedSubTask subTask : snapshot) {
                subTask.task().cancel();
            }
        }

        private boolean stepSpecificSubTasks(final Set<TaskId> taskIds,
                                             final RootStorage rootStorage,
                                             final ExternalPatternSinkProvider sinkProvider,
                                             final StepBehavior stepBehavior,
                                             final TaskListener listener) {
            boolean changed = false;
            for (final TaskId taskId : taskIds) {
                final DispatchedSubTask subTask = activeSubTasks.get(taskId);
                if (subTask == null) {
                    continue;
                }
                changed |= subTask.task().step(rootStorage, sinkProvider, stepBehavior, listener);
            }
            return changed;
        }

        private boolean dispatchStrict(final RootStorage rootStorage) {
            if (!activeSubTasks.isEmpty() || pendingSteps.isEmpty()) {
                return false;
            }

            final LpExecutionPlanStep next = pendingSteps.getFirst();
            final Map<ResourceKey, Long> available = availableWithReservations(rootStorage);
            final long dispatchIterations = maxDispatchableIterations(next, available);
            if (dispatchIterations <= 0) {
                return false;
            }
            final Map<ResourceKey, Long> requirements = stepRequirements(next, dispatchIterations);

            final Optional<TaskId> dispatched = dispatchSubTask(next, dispatchIterations, requirements);
            if (dispatched.isEmpty()) {
                return false;
            }

            updatePendingStepAfterDispatch(0, next, dispatchIterations);
            return true;
        }

        private boolean dispatchRelaxed(final RootStorage rootStorage) {
            if (pendingSteps.isEmpty()) {
                return false;
            }

            final Map<ResourceKey, Long> available = availableWithReservations(rootStorage);
            boolean changed = false;
            boolean dispatchedAny;
            do {
                dispatchedAny = false;
                for (int index = 0; index < pendingSteps.size(); index++) {
                    final LpExecutionPlanStep step = pendingSteps.get(index);
                    final long dispatchIterations = maxDispatchableIterations(step, available);
                    if (dispatchIterations <= 0) {
                        continue;
                    }
                    final Map<ResourceKey, Long> requirements = stepRequirements(step, dispatchIterations);

                    final Optional<TaskId> dispatched = dispatchSubTask(step, dispatchIterations, requirements);
                    if (dispatched.isEmpty()) {
                        continue;
                    }

                    consume(requirements, available);
                    updatePendingStepAfterDispatch(index, step, dispatchIterations);
                    changed = true;
                    dispatchedAny = true;
                    break;
                }
            } while (dispatchedAny);

            return changed;
        }

        private void updatePendingStepAfterDispatch(final int index,
                                                    final LpExecutionPlanStep originalStep,
                                                    final long dispatchedIterations) {
            final long remainingIterations = originalStep.iterations() - dispatchedIterations;
            if (remainingIterations <= 0) {
                pendingSteps.remove(index);
                return;
            }
            pendingSteps.set(index, new LpExecutionPlanStep(originalStep.recipe(), remainingIterations));
        }

        private Optional<TaskId> dispatchSubTask(final LpExecutionPlanStep step,
                                                 final long dispatchIterations,
                                                 final Map<ResourceKey, Long> requirements) {
            final LpExecutionPlanStep dispatchedStep = new LpExecutionPlanStep(step.recipe(), dispatchIterations);
            final boolean root = step.recipe().pattern().layout().outputs().stream()
                .anyMatch(output -> output.resource().equals(getResource()));
            final TaskPlan plan = toSingleStepPlan(getResource(), -1, dispatchedStep, root);
            final Pattern pattern = step.recipe().pattern();
            final PatternProvider provider = providerByPattern.get(pattern);
            if (provider == null) {
                return Optional.empty();
            }

            final SeededSubTask seededSubTask = createSeededSubTask(plan, requirements);
            activeSubTasks.put(
                seededSubTask.task().getId(),
                new DispatchedSubTask(seededSubTask.task(), seededSubTask.remainingRequirements(), root)
            );
            return Optional.of(seededSubTask.task().getId());
        }

        private SeededSubTask createSeededSubTask(final TaskPlan plan,
                                                  final Map<ResourceKey, Long> requirements) {
            final MutableResourceList seededInternalStorage = MutableResourceListImpl.create();
            final MutableResourceList remainingInitialRequirements = MutableResourceListImpl.create();

            requirements.forEach((resource, amountNeeded) -> {
                final long bufferedAmount = bufferedInternalStorage.get(resource);
                final long consumedFromBuffer = Math.min(bufferedAmount, amountNeeded);
                if (consumedFromBuffer > 0) {
                    bufferedInternalStorage.remove(resource, consumedFromBuffer);
                    seededInternalStorage.add(resource, consumedFromBuffer);
                }

                final long remaining = amountNeeded - consumedFromBuffer;
                if (remaining > 0) {
                    remainingInitialRequirements.add(resource, remaining);
                }
            });

            final TaskImpl baseTask = new TaskImpl(plan, getActor(), false);
            final TaskSnapshot baseSnapshot = baseTask.createSnapshot();
            final TaskState initialState = remainingInitialRequirements.isEmpty() ? TaskState.RUNNING : TaskState.READY;
            final TaskImpl task = new TaskImpl(new TaskSnapshot(
                baseSnapshot.id(),
                baseSnapshot.resource(),
                baseSnapshot.amount(),
                baseSnapshot.actor(),
                baseSnapshot.notifyActor(),
                baseSnapshot.startTime(),
                baseSnapshot.patterns(),
                baseSnapshot.completedPatterns(),
                remainingInitialRequirements.copy(),
                seededInternalStorage.copy(),
                initialState,
                false
            ));

            final Map<ResourceKey, Long> remainingRequirements = new HashMap<>();
            remainingInitialRequirements.getAll().forEach(resource ->
                remainingRequirements.put(resource, remainingInitialRequirements.get(resource)));
            return new SeededSubTask(task, remainingRequirements);
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public TaskStatus getStatus() {
            final TaskStatusBuilder builder = new TaskStatusBuilder(
                getId(),
                state,
                getResource(),
                getAmount(),
                startTime
            );
            if (strictOrdering) {
                builder.processing(getResource(), Math.max(1, activeSubTasks.size() + pendingSteps.size()), null);
            } else if (!pendingSteps.isEmpty()) {
                builder.scheduled(getResource(), pendingSteps.size());
            }
            return builder.build(progress());
        }

        @Override
        public TaskSnapshot createSnapshot() {
            final MutableResourceList internalStorage = MutableResourceListImpl.create();
            final MutableResourceList initialRequirements = MutableResourceListImpl.create();

            bufferedInternalStorage.getAll().forEach(resource ->
                internalStorage.add(resource, bufferedInternalStorage.get(resource)));

            for (final DispatchedSubTask subTask : activeSubTasks.values()) {
                final TaskSnapshot snapshot = subTask.task().createSnapshot();
                final MutableResourceList subTaskInternalStorage = snapshot.copyInternalStorage();
                subTaskInternalStorage.getAll().forEach(resource ->
                    internalStorage.add(resource, subTaskInternalStorage.get(resource)));
                snapshot.initialRequirements().getAll().forEach(resource ->
                    initialRequirements.add(resource, snapshot.initialRequirements().get(resource)));
            }

            return new TaskSnapshot(
                getId(),
                getResource(),
                getAmount(),
                getActor(),
                shouldNotify(),
                startTime,
                Map.of(),
                List.of(),
                initialRequirements.copy(),
                internalStorage.copy(),
                state,
                cancelled
            );
        }

        @Override
        public long beforeInsert(final ResourceKey insertedResource, final long insertedAmount) {
            if (cancelled) {
                return 0;
            }

            long intercepted = 0;
            for (final DispatchedSubTask subTask : activeSubTasks.values()) {
                final long available = insertedAmount - intercepted;
                intercepted += subTask.task().beforeInsert(insertedResource, available);
                if (intercepted == insertedAmount) {
                    break;
                }
            }

            final long remaining = insertedAmount - intercepted;
            final long pendingNeed = getPendingRequirement(insertedResource)
                - bufferedInternalStorage.get(insertedResource)
                - getActiveInternalStorage(insertedResource);
            if (remaining > 0 && pendingNeed > 0) {
                final long buffered = Math.min(remaining, pendingNeed);
                bufferedInternalStorage.add(insertedResource, buffered);
                intercepted += buffered;
            }
            return intercepted;
        }

        @Override
        public long afterInsert(final ResourceKey insertedResource, final long insertedAmount) {
            long reserved = 0;
            for (final DispatchedSubTask subTask : activeSubTasks.values()) {
                final long available = insertedAmount - reserved;
                reserved += subTask.task().afterInsert(insertedResource, available);
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

        private double progress() {
            if (totalSteps == 0) {
                return 1D;
            }
            final int remaining = pendingSteps.size() + activeSubTasks.size();
            final int done = Math.max(0, totalSteps - remaining);
            return done / (double) totalSteps;
        }

        private Map<ResourceKey, Long> availableWithReservations(final RootStorage rootStorage) {
            final Map<ResourceKey, Long> available = new HashMap<>();
            for (final ResourceAmount resourceAmount : rootStorage.getAll()) {
                available.put(resourceAmount.resource(), resourceAmount.amount());
            }
            bufferedInternalStorage.getAll().forEach(resource ->
                available.put(resource, available.getOrDefault(resource, 0L) + bufferedInternalStorage.get(resource)));
            for (final DispatchedSubTask subTask : activeSubTasks.values()) {
                consume(subTask.requirements(), available);
            }
            return available;
        }

        private long getPendingRequirement(final ResourceKey resource) {
            long amount = 0;
            for (final LpExecutionPlanStep step : pendingSteps) {
                for (final var ingredient : step.recipe().pattern().layout().ingredients()) {
                    if (ingredient.inputs().getFirst().equals(resource)) {
                        amount += ingredient.amount() * step.iterations();
                    }
                }
            }
            return amount;
        }

        private long getActiveInternalStorage(final ResourceKey resource) {
            long amount = 0;
            for (final DispatchedSubTask subTask : activeSubTasks.values()) {
                amount += subTask.task().createSnapshot().copyInternalStorage().get(resource);
            }
            return amount;
        }

        private static void consume(final Map<ResourceKey, Long> requirements,
                                    final Map<ResourceKey, Long> available) {
            for (final Map.Entry<ResourceKey, Long> entry : requirements.entrySet()) {
                available.put(entry.getKey(), available.getOrDefault(entry.getKey(), 0L) - entry.getValue());
            }
        }

        private static boolean canFulfill(final Map<ResourceKey, Long> requirements,
                                          final Map<ResourceKey, Long> available) {
            for (final Map.Entry<ResourceKey, Long> entry : requirements.entrySet()) {
                if (available.getOrDefault(entry.getKey(), 0L) < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        private static long maxDispatchableIterations(final LpExecutionPlanStep step,
                                                      final Map<ResourceKey, Long> available) {
            long maxIterations = step.iterations();
            final Pattern pattern = step.recipe().pattern();
            for (final var ingredient : pattern.layout().ingredients()) {
                final long perIterationAmount = ingredient.amount();
                if (perIterationAmount <= 0) {
                    continue;
                }
                final ResourceKey ingredientResource = ingredient.inputs().getFirst();
                final long availableAmount = available.getOrDefault(ingredientResource, 0L);
                maxIterations = Math.min(maxIterations, availableAmount / perIterationAmount);
                if (maxIterations <= 0) {
                    return 0;
                }
            }
            return Math.max(0, maxIterations);
        }

        private static Map<ResourceKey, Long> stepRequirements(final LpExecutionPlanStep step,
                                                               final long iterations) {
            final Map<ResourceKey, Long> requirements = new HashMap<>();
            final Pattern pattern = step.recipe().pattern();
            for (final var ingredient : pattern.layout().ingredients()) {
                final ResourceKey ingredientResource = ingredient.inputs().getFirst();
                requirements.merge(ingredientResource, ingredient.amount() * iterations, Long::sum);
            }
            return requirements;
        }

        private record DispatchedSubTask(TaskImpl task, Map<ResourceKey, Long> requirements, boolean root) {
        }

        private record SeededSubTask(TaskImpl task, Map<ResourceKey, Long> remainingRequirements) {
        }
    }
}
