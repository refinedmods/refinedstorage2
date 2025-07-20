package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // a network merge does not call onContainerRemoved, so call it here to avoid leaking listeners from old networks
    @Override
    public void onNetworkMergedWith(final Network newMainNetwork) {
        providers.forEach(provider -> provider.onRemovedFromContainer(this));
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
        return CompletableFuture.supplyAsync(() -> {
            final RootStorage rootStorage = rootStorageProvider.get();
            final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage,
                cancellationToken);
            final Preview preview = PreviewCraftingCalculatorListener.calculatePreview(calculator, resource, amount);
            return Optional.of(preview);
        }, executorService);
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        CoreValidations.validateNotNull(resource, "Resource cannot be null");
        return CompletableFuture.supplyAsync(() -> getMaxAmountSync(resource, cancellationToken), executorService);
    }

    private long getMaxAmountSync(final ResourceKey resource, final CancellationToken cancellationToken) {
        final RootStorage rootStorage = rootStorageProvider.get();
        final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage,
            cancellationToken);
        return calculator.getMaxAmount(resource);
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify,
                                                         final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        return CompletableFuture.supplyAsync(() -> startTaskSync(resource, amount, actor, notify, cancellationToken),
            executorService);
    }

    private TaskId startTask(final ResourceKey resource,
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

    private Optional<TaskId> startTaskSync(final ResourceKey resource,
                                           final long amount,
                                           final Actor actor,
                                           final boolean notify,
                                           final CancellationToken cancellationToken) {
        final RootStorage rootStorage = rootStorageProvider.get();
        final CraftingCalculator calculator = new CraftingCalculatorImpl(patternRepository, rootStorage,
            cancellationToken);
        return calculatePlan(calculator, resource, amount)
            .map(plan -> startTask(resource, amount, actor, plan, notify));
    }

    @Override
    public EnsureResult ensureTask(final ResourceKey resource, final long amount, final Actor actor) {
        ResourceAmount.validate(resource, amount);
        final long currentlyCrafting = providers.stream()
            .mapToLong(provider -> provider.getAmount(resource))
            .sum();
        if (currentlyCrafting >= amount) {
            return EnsureResult.TASK_ALREADY_RUNNING;
        }
        final long correctedAmount = Math.min(
            getMaxAmountSync(resource, CancellationToken.NONE),
            amount - currentlyCrafting
        );
        if (correctedAmount <= 0) {
            return EnsureResult.MISSING_RESOURCES;
        }
        return startTaskSync(resource, correctedAmount, actor, false, CancellationToken.NONE)
            .map(taskId -> EnsureResult.TASK_CREATED)
            .orElse(EnsureResult.MISSING_RESOURCES);
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
    public void cancel() {
        // no op
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
}
