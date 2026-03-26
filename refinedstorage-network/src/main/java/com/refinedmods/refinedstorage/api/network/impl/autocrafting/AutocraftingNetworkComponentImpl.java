package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorImpl;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpCraftingSolver;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPatternRecipe;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpResourceSet;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewCraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
        if (shouldUseOldSystem(resource)) {
            return calculatePlan(calculator, resource, amount, cancellationToken)
                .map(plan -> addTask(resource, amount, actor, plan, notify));
        } else {
            return calculateLpPlan(rootStorage, resource, amount, cancellationToken)
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
        if (shouldUseOldSystem(resource)) {
            final CraftingCalculatorImpl calculator = new CraftingCalculatorImpl(patternRepository, rootStorage);
            return calculatePlan(calculator, resource, correctedAmount, cancellationToken)
                .map(plan -> addTask(resource, correctedAmount, actor, plan, false))
                .map(taskId -> EnsureResult.TASK_CREATED)
                .orElseGet(() -> ensureTaskForCraftableAmount(resource, actor, correctedAmount, calculator,
                    cancellationToken));
        } else {
            return calculateLpPlan(rootStorage, resource, correctedAmount, cancellationToken)
                .map(plan -> addTask(resource, correctedAmount, actor, plan, false))
                .map(taskId -> EnsureResult.TASK_CREATED)
                .orElseGet(() -> ensureTaskForCraftableAmountViaLp(
                    rootStorage,
                    resource,
                    correctedAmount,
                    actor,
                    cancellationToken
                ));
        }
    }

    private boolean shouldUseOldSystem(final ResourceKey requestedResource) {
        // return true if any recipe in the crafting tree is fuzzy 
        // (has an ingredient with multiple possible inputs),
        // as the old system can handle this but the LP system cannot
        final Set<ResourceKey> visitedResources = new HashSet<>();
        final ArrayDeque<ResourceKey> resourcesToVisit = new ArrayDeque<>();
        resourcesToVisit.add(requestedResource);

        while (!resourcesToVisit.isEmpty()) {
            final ResourceKey currentResource = resourcesToVisit.removeFirst();
            if (!visitedResources.add(currentResource)) {
                continue;
            }

            for (final Pattern pattern : patternRepository.getByOutput(currentResource)) {
                if (pattern.layout().ingredients().stream().anyMatch(ingredient -> ingredient.inputs().size() != 1)) {
                    return true;
                }

                pattern.layout().ingredients().forEach(ingredient ->
                    resourcesToVisit.addLast(ingredient.inputs().getFirst())
                );
            }
        }

        return false;
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

    private Optional<TaskPlan> calculateLpPlan(final RootStorage rootStorage,
                                               final ResourceKey resource,
                                               final long amount,
                                               final CancellationToken cancellationToken) {
        if (cancellationToken.isCancelled()) {
            return Optional.empty();
        }

        final List<LpPatternRecipe> recipes = buildLpRecipes();
        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        final LpCraftingSolver.PlanningOutcome outcome = new LpCraftingSolver().solve(
            recipes,
            buildLpStartingResources(rootStorage),
            buildTarget(rootStorage, resource, amount)
        );
        return toTaskPlan(resource, amount, outcome);
    }

    private EnsureResult ensureTaskForCraftableAmountViaLp(final RootStorage rootStorage,
                                                           final ResourceKey resource,
                                                           final long amount,
                                                           final Actor actor,
                                                           final CancellationToken cancellationToken) {
        if (cancellationToken.isCancelled()) {
            return EnsureResult.MISSING_RESOURCES;
        }

        final long correctedAmount = findMaxCraftableAmountViaLp(
            rootStorage,
            resource,
            amount,
            cancellationToken
        );
        if (correctedAmount <= 0) {
            return EnsureResult.MISSING_RESOURCES;
        }

        return calculateLpPlan(rootStorage, resource, correctedAmount, cancellationToken)
            .map(plan -> addTask(resource, correctedAmount, actor, plan, false))
            .map(taskId -> EnsureResult.TASK_CREATED)
            .orElse(EnsureResult.MISSING_RESOURCES);
    }

    private long findMaxCraftableAmountViaLp(final RootStorage rootStorage,
                                             final ResourceKey resource,
                                             final long amount,
                                             final CancellationToken cancellationToken) {
        long low = 1;
        long high = amount;
        long best = 0;

        while (low <= high && !cancellationToken.isCancelled()) {
            final long middle = low + ((high - low) / 2);
            final boolean craftable = calculateLpPlan(rootStorage, resource, middle, cancellationToken).isPresent();
            if (craftable) {
                best = middle;
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }

        return best;
    }

    private List<LpPatternRecipe> buildLpRecipes() {
        final List<Pattern> patterns = patternRepository.getAll().stream()
            .sorted(Comparator.comparing(Pattern::id))
            .toList();
        final List<LpPatternRecipe> recipes = new ArrayList<>();
        for (int index = 0; index < patterns.size(); index++) {
            try {
                recipes.add(LpPatternRecipe.fromPattern(patterns.get(index), index));
            } catch (final IllegalArgumentException e) {
                LOGGER.debug("Skipping LP-incompatible pattern {}", patterns.get(index), e);
            }
        }
        return recipes;
    }

    private static LpResourceSet buildLpStartingResources(final RootStorage rootStorage) {
        return LpResourceSet.fromResourceAmounts(rootStorage.getAll());
    }

    private static LpResourceSet buildTarget(final RootStorage rootStorage,
                                             final ResourceKey resource,
                                             final long amount) {
        final LpResourceSet target = new LpResourceSet();
        target.setAmount(resource, rootStorage.get(resource) + amount);
        return target;
    }

    private Optional<TaskPlan> toTaskPlan(final ResourceKey resource,
                                          final long amount,
                                          final LpCraftingSolver.PlanningOutcome outcome) {
        if (outcome.executableResult().isEmpty()) {
            return Optional.empty();
        }

        final List<LpExecutionPlanStep> steps = outcome.executableResult().get().plan();
        final Pattern rootPattern = findRootPattern(resource, steps);
        if (rootPattern == null) {
            return Optional.empty();
        }

        final Map<Pattern, PatternPlanAccumulator> accumulators = new LinkedHashMap<>();
        for (final LpExecutionPlanStep step : steps) {
            final Pattern pattern = step.recipe().pattern();
            final PatternPlanAccumulator accumulator = accumulators.computeIfAbsent(
                pattern,
                ignored -> new PatternPlanAccumulator(pattern.equals(rootPattern))
            );
            accumulator.addIterations(step.iterations());
            addIngredientUsage(accumulator, pattern, step.iterations());
        }

        final Map<Pattern, TaskPlan.PatternPlan> patterns = new LinkedHashMap<>();
        accumulators.forEach((pattern, accumulator) -> patterns.put(pattern, accumulator.toPlan()));
        return Optional.of(new TaskPlan(
            resource,
            amount,
            rootPattern,
            patterns,
            computeInitialRequirements(steps, rootPattern)
        ));
    }

    private static void addIngredientUsage(final PatternPlanAccumulator accumulator,
                                           final Pattern pattern,
                                           final long iterations) {
        for (int ingredientIndex = 0; ingredientIndex < pattern.layout().ingredients().size(); ingredientIndex++) {
            final var ingredient = pattern.layout().ingredients().get(ingredientIndex);
            final ResourceKey ingredientResource = ingredient.inputs().getFirst();
            accumulator.addIngredient(ingredientIndex, ingredientResource, ingredient.amount() * iterations);
        }
    }

    private static List<ResourceAmount> computeInitialRequirements(final List<LpExecutionPlanStep> steps,
                                                                   final Pattern rootPattern) {
        final Map<ResourceKey, Long> internalStorage = new LinkedHashMap<>();
        final Map<ResourceKey, Long> initialRequirements = new LinkedHashMap<>();

        for (final LpExecutionPlanStep step : steps) {
            final Pattern pattern = step.recipe().pattern();
            for (int iteration = 0; iteration < step.iterations(); iteration++) {
                consumeIterationInputs(pattern, internalStorage, initialRequirements);
                if (!pattern.equals(rootPattern)) {
                    addIterationOutputs(pattern, internalStorage);
                }
            }
        }

        return initialRequirements.entrySet().stream()
            .map(entry -> new ResourceAmount(entry.getKey(), entry.getValue()))
            .toList();
    }

    private static void consumeIterationInputs(final Pattern pattern,
                                               final Map<ResourceKey, Long> internalStorage,
                                               final Map<ResourceKey, Long> initialRequirements) {
        pattern.layout().ingredients().forEach(ingredient -> {
            final ResourceKey resource = ingredient.inputs().getFirst();
            final long amount = ingredient.amount();
            final long available = internalStorage.getOrDefault(resource, 0L);
            final long fromInternalStorage = Math.min(available, amount);
            final long missing = amount - fromInternalStorage;
            if (fromInternalStorage > 0) {
                internalStorage.put(resource, available - fromInternalStorage);
            }
            if (missing > 0) {
                initialRequirements.merge(resource, missing, Long::sum);
            }
        });
    }

    private static void addIterationOutputs(final Pattern pattern,
                                            final Map<ResourceKey, Long> internalStorage) {
        pattern.layout().outputs().forEach(output ->
            internalStorage.merge(output.resource(), output.amount(), Long::sum));
        pattern.layout().byproducts().forEach(byproduct ->
            internalStorage.merge(byproduct.resource(), byproduct.amount(), Long::sum));
    }

    private static Pattern findRootPattern(final ResourceKey resource,
                                           final List<LpExecutionPlanStep> steps) {
        for (int index = steps.size() - 1; index >= 0; index--) {
            final Pattern pattern = steps.get(index).recipe().pattern();
            final boolean producesResource = pattern.layout().outputs().stream()
                .anyMatch(output -> output.resource().equals(resource));
            if (producesResource) {
                return pattern;
            }
        }
        return null;
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

    private static final class PatternPlanAccumulator {
        private final boolean root;
        private final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();
        private long iterations;

        private PatternPlanAccumulator(final boolean root) {
            this.root = root;
        }

        private void addIterations(final long additionalIterations) {
            this.iterations += additionalIterations;
        }

        private void addIngredient(final int ingredientIndex,
                                   final ResourceKey resource,
                                   final long amount) {
            ingredients.computeIfAbsent(ingredientIndex, ignored -> new LinkedHashMap<>())
                .merge(resource, amount, Long::sum);
        }

        private TaskPlan.PatternPlan toPlan() {
            final Map<Integer, Map<ResourceKey, Long>> copiedIngredients = new LinkedHashMap<>();
            ingredients.forEach((ingredientIndex, resources) ->
                copiedIngredients.put(ingredientIndex, Map.copyOf(new LinkedHashMap<>(resources))));
            return new TaskPlan.PatternPlan(root, iterations, Map.copyOf(copiedIngredients));
        }
    }
}
