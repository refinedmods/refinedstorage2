package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpCraftingSolver;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPatternRecipe;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpResourceSet;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpDispatcherHelper;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPlanningHelper;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpStepPlan;
import com.refinedmods.refinedstorage.network.test.fixtures.NetworkTestFixtures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutocraftingNetworkComponentImplTest {
    private Network network;
    private RootStorage rootStorage;
    private AutocraftingNetworkComponentImpl sut;

    @BeforeEach
    void setUp() {
        network = new NetworkImpl(NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY);
        rootStorage = network.getComponent(StorageNetworkComponent.class);
        sut = new AutocraftingNetworkComponentImpl(() -> rootStorage, Executors.newSingleThreadExecutor());
    }

    @Test
    void shouldAddPatternsFromPatternProvider() {
        // Arrange
        final PatternBuilder patternABuilder = pattern().output(A, 1).ingredient(C, 1);
        final PatternBuilder patternBBuilder = pattern().output(B, 1).ingredient(C, 1);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern1 = patternABuilder.build();
        provider1.setPattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.setPattern(1, pattern2);
        provider2.setPattern(2, pattern3);

        // Act
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider2);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(A, B);
        assertThat(sut.getProviderByPattern(pattern1)).isEqualTo(provider1);
        assertThat(sut.getProviderByPattern(pattern2)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(pattern3)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(patternABuilder.build())).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).containsExactlyInAnyOrder(provider1, provider2);
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).containsExactlyInAnyOrder(provider1, provider2);
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern().output(D, 1).ingredient(C, 1).buildLayout()))
            .isEmpty();
    }

    @Test
    void shouldRemovePatternsFromPatternProvider() {
        // Arrange
        final PatternBuilder patternABuilder = pattern().output(A, 1).ingredient(C, 1);
        final PatternBuilder patternBBuilder = pattern().output(B, 1).ingredient(C, 1);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern1 = patternABuilder.build();
        provider1.setPattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.setPattern(1, pattern2);
        provider2.setPattern(2, pattern3);

        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider1);
        sut.onContainerAdded(() -> provider2);

        // Act & assert
        sut.onContainerRemoved(() -> provider1);
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(A, B);
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isEqualTo(provider2);
        assertThat(sut.getProviderByPattern(pattern3)).isEqualTo(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).containsExactly(provider2);
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).containsExactly(provider2);

        sut.onContainerRemoved(() -> provider2);
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isNull();
        assertThat(sut.getProviderByPattern(pattern3)).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).isEmpty();

        sut.onContainerRemoved(() -> provider2);
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getProviderByPattern(pattern1)).isNull();
        assertThat(sut.getProviderByPattern(pattern2)).isNull();
        assertThat(sut.getProviderByPattern(pattern3)).isNull();
        assertThat(sut.getSinksByPatternLayout(pattern1.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern2.layout())).isEmpty();
        assertThat(sut.getSinksByPatternLayout(pattern3.layout())).isEmpty();
    }

    @Test
    void shouldGetPreview() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<Preview> preview = sut.getPreview(B, 2, CancellationToken.NONE).join();

        // Assert
        assertThat(preview).get().usingRecursiveComparison().isEqualTo(new Preview(PreviewType.SUCCESS, List.of(
            new PreviewItem(B, 0, 0, 2),
            new PreviewItem(A, 6, 0, 0)
        ), Collections.emptyList()));
    }

    @Test
    void shouldNotGetPreviewIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<Preview> preview = sut.getPreview(B, 2, new CancelledCancellationToken()).join();

        // Assert
        assertThat(preview).isPresent();
        assertThat(preview.get().type()).isEqualTo(PreviewType.CANCELLED);
    }


    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotGetPreviewForInvalidResource() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getPreview(null, 1, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotGetPreviewForInvalidAmount(final long amount) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getPreview(B, amount, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldGetTreePreview() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TreePreview> preview = sut.getTreePreview(B, 2, CancellationToken.NONE).join();

        // Assert
        assertThat(preview).get().usingRecursiveComparison().isEqualTo(new TreePreview(PreviewType.SUCCESS,
            new TreePreviewNode(B, 2, 2, 0, 0, List.of(
                new TreePreviewNode(A, 6, 0, 6, 0, Collections.emptyList())
            )), Collections.emptyList()));
    }

    @Test
    void shouldNotGetTreePreviewIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TreePreview> preview = sut.getTreePreview(B, 2, new CancelledCancellationToken()).join();

        // Assert
        assertThat(preview).isPresent();
        assertThat(preview.get().type()).isEqualTo(PreviewType.CANCELLED);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotGetTreePreviewForInvalidResource() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getTreePreview(null, 1, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotGetTreePreviewForInvalidAmount(final long amount) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getTreePreview(B, amount, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldGetMaxAmount() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 64, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 4).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final long maxAmount = sut.getMaxAmount(B, CancellationToken.NONE).join();

        // Assert
        assertThat(maxAmount).isEqualTo(16);
    }

    @Test
    void shouldNotGetMaxAmountIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 64, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 4).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final long maxAmount = sut.getMaxAmount(B, new CancelledCancellationToken()).join();

        // Assert
        assertThat(maxAmount).isZero();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotGetMaxAmountForInvalidResource() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.getMaxAmount(null, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldStartTask() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(taskId).isPresent();
        assertThat(provider.getTasks()).hasSize(1);
    }

    @Test
    void shouldUseLpPlanningAlgorithmWhenResourceAppearsInMultipleIngredients() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(B, 1).ingredient(B, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(B)).isEqualTo(PlanningAlgorithm.LP);
    }

    @Test
    void shouldTreatRequestedAmountAsAdditionalAmountForSelfConsumingRecipe() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(A, 4, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(B, 1).ingredient(B, 1).output(B, 1).build());
        final Pattern duplicationPattern = pattern().ingredient(B, 1).ingredient(A, 1).output(B, 2).build();
        provider.setPattern(1, duplicationPattern);
        sut.onContainerAdded(() -> provider);

        // Act
        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            4,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(taskId).isPresent();
        assertThat(provider.getTasks()).hasSize(1);
        assertThat(provider.getTasks().getFirst().getAmount()).isEqualTo(4);

        final TaskImpl task = (TaskImpl) provider.getTasks().getFirst();
        final var snapshot = task.createSnapshot();
        assertThat(snapshot.patterns()).containsKey(duplicationPattern);
        assertThat(snapshot.patterns().get(duplicationPattern).internalPattern()).isNotNull();
        assertThat(snapshot.patterns().get(duplicationPattern).internalPattern().iterationsRemaining())
            .isEqualTo(4);
    }

    @Test
    void shouldUseTraditionalPlanningAlgorithmWhenPatternHasFuzzyInputs() {
        // Arrange
        // Both alternatives (A and B) exist in storage, so the ingredient is genuinely fuzzy.
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(B)).isEqualTo(PlanningAlgorithm.TRADITIONAL);
    }

    @Test
    void shouldUseLpPlanningAlgorithmWhenPatternHasFuzzyInputsButOnlyOneAlternativeIsAvailable() {
        // Arrange
        // Only A exists in storage; B is neither in storage nor craftable.
        // The ingredient [A, B] effectively has only one viable option, so LP can be used.
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.LP);
    }

    @Test
    void shouldUseLpPlanningAlgorithmWhenPatternHasFuzzyInputsButOnlyOneAlternativeIsCraftable() {
        // Arrange
        // A has a pattern (craftable); B is neither in storage nor craftable.
        // The ingredient [A, B] effectively has only one viable option, so LP can be used.
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        provider.setPattern(2, pattern().ingredient(D, 1).output(A, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.LP);
    }

    @Test
    void shouldUseTraditionalPlanningAlgorithmWhenPatternHasFuzzyInputsAndOneAlternativeIsInStorageAndOneIsCraftable() {
        // Arrange
        // A exists in storage; B is craftable via a pattern.
        // Both alternatives are viable, so the ingredient is genuinely fuzzy → use traditional.
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        provider.setPattern(2, pattern().ingredient(D, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.TRADITIONAL);
    }

    @Test
    void shouldUseTraditionalPlanningAlgorithmWhenPatternHasFuzzyInputsAndBothAlternativesAreCraftable() {
        // Arrange
        // Both A and B are craftable via patterns; neither is in storage.
        // Both alternatives are viable, so the ingredient is genuinely fuzzy → use traditional.
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        provider.setPattern(2, pattern().ingredient(D, 1).output(A, 1).build());
        provider.setPattern(3, pattern().ingredient(D, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.TRADITIONAL);
    }

    @Test
    void shouldUseTraditionalPlanningAlgorithmWhenRelevantDependencyPatternHasFuzzyInputs() {
        // Arrange
        // Both alternatives (A and D) exist in storage, so the dependency ingredient is genuinely fuzzy.
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(D, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(B, 1).output(C, 1).build());
        provider.setPattern(2, pattern().ingredient(1).input(A).input(D).end().output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.TRADITIONAL);
    }

    @Test
    void shouldUseTraditionalPlanningAlgorithmWhenOnlyViableInputHasFuzzyDependency() {
        // Arrange
        // For C, the root ingredient [A, B] has only A viable (B is unavailable).
        // A itself is crafted by a pattern with fuzzy [D, E], both in storage, so the tree is genuinely fuzzy.
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(D, 1, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(A_ALTERNATIVE, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        provider.setPattern(2, pattern().ingredient(1).input(D).input(A_ALTERNATIVE).end().output(A, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(C)).isEqualTo(PlanningAlgorithm.TRADITIONAL);
    }

    @Test
    void shouldUseLpPlanningAlgorithmForRegularRecipeShapes() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(determinePlanningAlgorithm(B)).isEqualTo(PlanningAlgorithm.LP);
    }

    @Test
    void shouldExecuteLpPlanningPathWhenResourceAppearsInMultipleIngredients() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(B, 1).ingredient(B, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = ensureTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.MISSING_RESOURCES);
        assertThat(provider.getTasks()).isEmpty();
    }

    @Test
    void shouldEnforceGeneratedOrderForCyclePlans() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        // Act
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        rootProvider.setActive(true);
        rootProvider.setNetwork(network);
        rootProvider.doWork();

        // Assert
        assertThat(dispatcherTask).isPresent();
        assertThat(rootProvider.getTasks()).hasSize(1);
        assertThat(blockedStepProvider.getTasks()).isEmpty();
    }

    @Test
    void shouldAllowLaterStepDispatchForAcyclicPlans() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        // Act
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        rootProvider.setActive(true);
        rootProvider.setNetwork(network);
        rootProvider.doWork();

        // Assert
        assertThat(dispatcherTask).isPresent();
        assertThat(blockedStepProvider.getTasks()).isEmpty();
        assertThat(rootProvider.getTasks())
            .hasSize(1)
            .allMatch(task -> task.getId().equals(dispatcherTask.get()));
        final TaskImpl dispatcher = (TaskImpl) rootProvider.getTasks().getFirst();
        assertThat(dispatcher.createSnapshot().copyInternalStorage().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(C, 1));
        assertThat(rootStorage.getAll()).isEmpty();
    }

    @Test
    void shouldSplitStepWhenOnlyPartCanStart() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        final Pattern expandableStepPattern = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(C, 1).build();

        final PatternProviderNetworkNode expandableStepProvider = new PatternProviderNetworkNode(0, 5);
        expandableStepProvider.setPattern(1, expandableStepPattern);
        sut.onContainerAdded(() -> expandableStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(expandableStepPattern, 0), 4),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        // Act
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(C, 1, steps, true);
        rootProvider.setActive(true);
        rootProvider.setNetwork(network);
        rootProvider.doWork();

        // Assert
        assertThat(dispatcherTask).isPresent();
        assertThat(rootProvider.getTasks()).hasSize(1);
        assertThat(expandableStepProvider.getTasks()).isEmpty();
        final TaskImpl dispatcher = (TaskImpl) rootProvider.getTasks().getFirst();
        assertThat(dispatcher.createSnapshot().copyInternalStorage().copyState())
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 2));
    }

    @Test
    void shouldNotNotifyWhenLpDispatcherIsCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        // Act
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true, true);

        // Assert
        assertThat(dispatcherTask).isPresent();
        assertThat(rootProvider.getTasks()).hasSize(1);
        final var dispatcher = rootProvider.getTasks().getFirst();
        assertThat(dispatcher.shouldNotify()).isTrue();

        dispatcher.cancel();
        assertThat(dispatcher.shouldNotify()).isFalse();
    }

    @Test
    void shouldDispatchFirstStrictStepWhenRequirementsAreAvailable() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern firstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstStepProvider = new PatternProviderNetworkNode(0, 5);
        firstStepProvider.setPattern(1, firstStepPattern);
        sut.onContainerAdded(() -> firstStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(firstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        // Assert
        assertThat(changed).isTrue();
        assertThat(firstStepProvider.getTasks()).isEmpty();
        assertThat(invokeDispatcherPendingRequirement(dispatcher, C)).isZero();
    }

    @Test
    void shouldNotDispatchStrictWhenFirstStepProviderIsMissing() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern unregisteredFirstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(unregisteredFirstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        // Assert
        assertThat(changed).isFalse();
    }

    @Test
    void shouldNotDispatchStrictWhenActiveSubTaskExists() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern firstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstStepProvider = new PatternProviderNetworkNode(0, 5);
        firstStepProvider.setPattern(1, firstStepPattern);
        sut.onContainerAdded(() -> firstStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(firstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        // Assert
        assertThat(changed).isFalse();
    }

    @Test
    void shouldDispatchLaterRelaxedStepWhenFirstStepCannotRun() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchRelaxed", rootStorage);

        // Assert
        assertThat(changed).isTrue();
        assertThat(blockedStepProvider.getTasks()).isEmpty();
        assertThat(rootProvider.getTasks()).hasSize(1);
        assertThat(invokeDispatcherPendingRequirement(dispatcher, C)).isZero();
        assertThat(invokeDispatcherPendingRequirement(dispatcher, A)).isEqualTo(4);
    }

    @Test
    void shouldNotDispatchRelaxedWhenPendingStepsAreEmpty() {
        // Arrange
        rootStorage.addSource(new StorageImpl());

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern blockedSecondStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode secondProvider = new PatternProviderNetworkNode(0, 5);
        secondProvider.setPattern(1, blockedSecondStepPattern);
        sut.onContainerAdded(() -> secondProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedSecondStepPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = secondProvider.getTasks().getFirst();
        clearDispatcherPendingSteps(dispatcher);

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchRelaxed", rootStorage);

        // Assert
        assertThat(changed).isFalse();
    }

    @Test
    void shouldNotDispatchRelaxedWhenNoStepCanRun() {
        // Arrange
        rootStorage.addSource(new StorageImpl());

        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern blockedSecondStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode secondProvider = new PatternProviderNetworkNode(0, 5);
        secondProvider.setPattern(1, blockedSecondStepPattern);
        sut.onContainerAdded(() -> secondProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedSecondStepPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = secondProvider.getTasks().getFirst();

        // Act
        final boolean changed = invokeDispatcherBooleanMethod(dispatcher, "dispatchRelaxed", rootStorage);

        // Assert
        assertThat(changed).isFalse();
        assertThat(firstProvider.getTasks()).isEmpty();
        assertThat(secondProvider.getTasks()).hasSize(1);
    }

    @Test
    void shouldFindRootPatternAtFirstStepIndex() {
        // Arrange
        final Pattern first = pattern().ingredient(A, 1).output(D, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();
        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(first, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(second, 1), 1)
        );

        // Act
        final Pattern root = invokeFindRootPattern(D, steps);

        // Assert
        assertThat(root).isEqualTo(first);
    }

    @Test
    void shouldReturnNullWhenNoRootPatternIsFound() {
        // Arrange
        final Pattern first = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern second = pattern().ingredient(B, 1).output(C, 1).build();
        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(first, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(second, 1), 1)
        );

        // Act
        final Pattern root = invokeFindRootPattern(D, steps);

        // Assert
        assertThat(root).isNull();
    }

    @Test
    void shouldCollectRelevantPatternsWithoutLoopingOnCycles() {
        // Arrange
        final Pattern patternA = pattern().ingredient(C, 1).output(A, 1).build();
        final Pattern patternC = pattern().ingredient(A, 1).output(C, 1).build();

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, patternA);
        provider.setPattern(2, patternC);
        sut.onContainerAdded(() -> provider);

        // Act
        final var relevant = invokeCollectRelevantPatternsForLp(C, rootStorage);

        // Assert
        assertThat(relevant).containsExactlyInAnyOrder(patternA, patternC);
    }

    @Test
    void shouldFindMaxCraftableAmountViaLpAtUpperBoundary() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 5, Action.EXECUTE, Actor.EMPTY);

        final Pattern patternAB = pattern().ingredient(A, 1).output(B, 1).build();
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, patternAB);
        sut.onContainerAdded(() -> provider);

        final var relevant = invokeCollectRelevantPatternsForLp(B, rootStorage);

        // Act
        final long maxCraftable = invokeFindMaxCraftableAmountViaLp(
            relevant,
            rootStorage,
            B,
            5,
            CancellationToken.NONE
        );

        // Assert
        assertThat(maxCraftable).isEqualTo(5);
    }

    @Test
    void shouldCollectRelevantPatternsTraversingFuzzyIngredients() {
        // Arrange: Exercise the `ingredient.inputs().size() > 1` branch in collectRelevantPatternsForLp
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        // Pattern: fuzzy ingredient with both A and A_ALTERNATIVE
        final Pattern fuzzyPattern = pattern()
            .ingredient(1).input(A).input(A_ALTERNATIVE).end()
            .output(B, 1)
            .build();
        // Pattern: A outputs C
        final Pattern patternAtoC = pattern().ingredient(A, 1).output(C, 1).build();

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 10);
        provider.setPattern(1, fuzzyPattern);
        provider.setPattern(2, patternAtoC);
        sut.onContainerAdded(() -> provider);

        // Act: Collect B → should include fuzzyPattern; patternAtoC included only if A is reachable via provider output
        final var relevant = invokeCollectRelevantPatternsForLp(B, rootStorage);

        // Assert: First, verify fuzzyPattern is collected (it outputs B)
        assertThat(relevant).contains(fuzzyPattern);
    }

    @Test
    void shouldFindMaxCraftableAmountViaLpWhenCancelledDuringBinarySearch() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        final Pattern patternAB = pattern().ingredient(A, 1).output(B, 1).build();
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, patternAB);
        sut.onContainerAdded(() -> provider);

        final var relevant = invokeCollectRelevantPatternsForLp(B, rootStorage);

        // Act: Binary search with early cancellation → should return 0
        final long maxCraftable = invokeFindMaxCraftableAmountViaLp(
            relevant,
            rootStorage,
            B,
            100,
            new CancelledCancellationToken()
        );

        // Assert
        assertThat(maxCraftable).isZero();
    }

    @Test
    void shouldNotInterceptBeforeInsertWhenDispatcherIsCancelled() {
        // Arrange
        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();
        dispatcher.cancel();

        // Act
        final long intercepted = dispatcher.beforeInsert(A, 10);

        // Assert
        assertThat(intercepted).isZero();
    }

    @Test
    void shouldOnlyInterceptUpToPendingRequirementInBeforeInsert() {
        // Arrange
        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern laterStepPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode blockedStepProvider = new PatternProviderNetworkNode(0, 5);
        blockedStepProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> blockedStepProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, laterStepPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(laterStepPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        // Act
        final long firstIntercepted = dispatcher.beforeInsert(A, 10);
        final long secondIntercepted = dispatcher.beforeInsert(A, 10);

        // Assert
        assertThat(firstIntercepted).isEqualTo(4);
        assertThat(secondIntercepted).isZero();
    }

    @Test
    void shouldUseRequestedAmountForSingleStepPlanWhenRequestedAmountIsPositive() {
        // Arrange
        final Pattern singleStepPattern = pattern().ingredient(A, 2).output(B, 3).build();
        final LpExecutionPlanStep step = new LpExecutionPlanStep(LpPatternRecipe.fromPattern(singleStepPattern, 0), 4);

        // Act
        final var plan = invokeToSingleStepPlan(B, 5, step, true);

        // Assert
        assertThat(plan.resource()).isEqualTo(B);
        assertThat(plan.amount()).isEqualTo(5);
        assertThat(plan.getPattern(singleStepPattern).iterations()).isEqualTo(4);
        assertThat(plan.getPattern(singleStepPattern).ingredients().get(0).get(A)).isEqualTo(8);
    }

    @Test
    void shouldUseOutputAmountForSingleStepPlanWhenRequestedAmountIsZero() {
        // Arrange
        final Pattern singleStepPattern = pattern().ingredient(A, 2).output(B, 3).build();
        final LpExecutionPlanStep step = new LpExecutionPlanStep(LpPatternRecipe.fromPattern(singleStepPattern, 0), 4);

        // Act
        final var plan = invokeToSingleStepPlan(B, 0, step, true);

        // Assert
        assertThat(plan.resource()).isEqualTo(B);
        assertThat(plan.amount()).isEqualTo(12);
    }

    @Test
    void shouldFallbackToRequestedResourceWhenSingleStepPatternHasNoOutputs() {
        // Arrange - use mocks because PatternBuilder requires at least one output
        final Pattern mockedPattern = mock(Pattern.class);
        final var mockedLayout = mock(com.refinedmods.refinedstorage.api.autocrafting.PatternLayout.class);

        when(mockedPattern.id()).thenReturn(java.util.UUID.randomUUID());
        when(mockedPattern.layout()).thenReturn(mockedLayout);
        when(mockedLayout.ingredients()).thenReturn(List.of());
        when(mockedLayout.outputs()).thenReturn(List.of());

        final LpPatternRecipe recipe = new LpPatternRecipe(
            mockedPattern,
            new com.refinedmods.refinedstorage.api.autocrafting.lp.LpResourceSet(),
            new com.refinedmods.refinedstorage.api.autocrafting.lp.LpResourceSet(),
            0,
            null
        );
        final LpExecutionPlanStep step = new LpExecutionPlanStep(recipe, 4);

        // Act
        final var plan = invokeToSingleStepPlan(C, 0, step, true);

        // Assert
        assertThat(plan.resource()).isEqualTo(C);
        assertThat(plan.amount()).isEqualTo(4);
    }

    @Test
    void shouldNotStartTaskIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            new CancelledCancellationToken(),
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(taskId).isEmpty();
        assertThat(provider.getTasks()).isEmpty();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotStartTaskForInvalidResource() {
        // Act
        final ThrowableAssert.ThrowingCallable action =
            () -> sut.startTask(null, 1, Actor.EMPTY, false, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotStartTaskForInvalidAmount(final long amount) {
        // Act
        final ThrowableAssert.ThrowingCallable action =
            () -> sut.startTask(B, amount, Actor.EMPTY, false, CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEnsureTask() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        )).isPresent();
        final var result = ensureTaskExpectingAlgorithm(
            B,
            10,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_CREATED);
        final var result2 = ensureTaskExpectingAlgorithm(
            B,
            10,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        assertThat(result2).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING);
        final var result3 = ensureTaskExpectingAlgorithm(
            B,
            9,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        assertThat(result3).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING);
        assertThat(provider.getTasks()).hasSize(2)
            .anyMatch(t -> t.getAmount() == 9)
            .anyMatch(t -> t.getAmount() == 1);
    }

    @Test
    void shouldNotEnsureTaskIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            new CancelledCancellationToken(),
            PlanningAlgorithm.LP
        )).isEmpty();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotEnsureTaskForInvalidResource() {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.ensureTask(null, 1, Actor.EMPTY,
            CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void shouldNotEnsureTaskForInvalidAmount(final long amount) {
        // Act
        final ThrowableAssert.ThrowingCallable action = () -> sut.ensureTask(B, amount, Actor.EMPTY,
            CancellationToken.NONE);

        // Act & assert
        assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotEnsureTaskWhenWeDontHaveEnoughResources() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = ensureTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.MISSING_RESOURCES);
        assertThat(provider.getTasks()).isEmpty();
    }

    @Test
    void shouldEnsureTaskEvenIfWeDontHaveEnoughResourcesForTheRequestedAmount() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = ensureTaskExpectingAlgorithm(
            B,
            11,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_CREATED);
        assertThat(provider.getTasks()).hasSize(1).allMatch(t -> t.getAmount() == 10);
    }

    @Test
    void shouldEnsureTaskEvenIfWeCouldTheoreticallyRequestMore() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 20, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = ensureTaskExpectingAlgorithm(
            B,
            11,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_CREATED);
        assertThat(provider.getTasks()).hasSize(1).allMatch(t -> t.getAmount() == 11);
    }

    @Test
    void shouldEnsureTaskWhenATaskIsAlreadyRunning() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Act
        final var result = ensureTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING);
    }

    @Test
    void shouldCancelTask() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setActive(true);
        provider1.setNetwork(network);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        final Optional<TaskId> taskId2 = startTaskExpectingAlgorithm(
            C,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

        // Act & assert
        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        sut.cancel(taskId1.get());
        assertThat(provider1.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        provider1.doWork();
        assertThat(provider1.getTasks()).isEmpty();
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        sut.cancel(taskId1.get());
    }

    @Test
    void shouldCancelAllTasks() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setActive(true);
        provider1.setNetwork(network);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        final Optional<TaskId> taskId2 = startTaskExpectingAlgorithm(
            C,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

        // Act & assert
        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(provider2.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RUNNING);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        sut.cancelAll();
        assertThat(provider1.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(provider2.getTasks()).hasSize(1)
            .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 4)
        );

        provider1.doWork();
        provider2.doWork();
        assertThat(provider1.getTasks()).isEmpty();
        assertThat(provider2.getTasks()).isEmpty();
        assertThat(rootStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );

        sut.cancelAll();
    }

    @Test
    void shouldNotStartTaskWhenThereAreMissingIngredients() {
        // Arrange
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            2,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        // Assert
        assertThat(taskId).isEmpty();
        assertThat(provider.getTasks()).isEmpty();
    }

    @Test
    void shouldGetAllTaskStatuses() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider1 = new PatternProviderNetworkNode(0, 5);
        provider1.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setPattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final PatternProviderNetworkNode provider3 = new PatternProviderNetworkNode(0, 5);
        provider3.setPattern(1, pattern().ingredient(A, 3).output(D, 1).build());
        sut.onContainerAdded(() -> provider3);

        final Optional<TaskId> taskId1 = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        final Optional<TaskId> taskId2 = startTaskExpectingAlgorithm(
            C,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        startTaskExpectingAlgorithm(
            D,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        sut.onContainerRemoved(() -> provider3);

        // Act
        final List<TaskStatus> taskStatuses = sut.getStatuses();

        // Assert
        assertThat(taskId1).isPresent();
        assertThat(taskId2).isPresent();
        assertThat(provider1.getTasks()).hasSize(1);
        assertThat(provider2.getTasks()).hasSize(1);
        assertThat(taskStatuses)
            .hasSize(2)
            .anyMatch(ts -> ts.info().id().equals(taskId1.get()))
            .anyMatch(ts -> ts.info().id().equals(taskId2.get()));
    }

    @Test
    void shouldReturnNotAvailablePreviewWhenExecutorRejects() {
        final var rejectedExecutor = Executors.newSingleThreadExecutor();
        rejectedExecutor.shutdownNow();
        final var localSut = new AutocraftingNetworkComponentImpl(() -> rootStorage, rejectedExecutor);

        final Optional<Preview> preview = localSut.getPreview(B, 1, CancellationToken.NONE).join();

        assertThat(preview).isPresent();
        assertThat(preview.get().type()).isEqualTo(PreviewType.NOT_AVAILABLE);
    }

    @Test
    void shouldReturnNotAvailableTreePreviewWhenExecutorRejects() {
        final var rejectedExecutor = Executors.newSingleThreadExecutor();
        rejectedExecutor.shutdownNow();
        final var localSut = new AutocraftingNetworkComponentImpl(() -> rootStorage, rejectedExecutor);

        final Optional<TreePreview> preview = localSut.getTreePreview(B, 1, CancellationToken.NONE).join();

        assertThat(preview).isPresent();
        assertThat(preview.get().type()).isEqualTo(PreviewType.NOT_AVAILABLE);
    }

    @Test
    void shouldStartTaskWithTraditionalPlanningAlgorithm() {
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        sut.onContainerAdded(() -> provider);

        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            C,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.TRADITIONAL
        );

        assertThat(taskId).isPresent();
        assertThat(provider.getTasks()).hasSize(1);
    }

    @Test
    void shouldEnsureTaskWithTraditionalPlanningAlgorithm() {
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 3, Action.EXECUTE, Actor.EMPTY);
        rootStorage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(1).input(A).input(B).end().output(C, 1).build());
        sut.onContainerAdded(() -> provider);

        final var result = ensureTaskExpectingAlgorithm(
            C,
            2,
            Actor.EMPTY,
            CancellationToken.NONE,
            PlanningAlgorithm.TRADITIONAL
        );

        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_CREATED);
        assertThat(provider.getTasks()).hasSize(1);
    }

    @Test
    void shouldExposeProcessingInStatusForStrictDispatcher() {
        final Pattern firstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, firstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(firstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        final TaskStatus status = dispatcher.getStatus();

        assertThat(status.percentageCompleted()).isZero();
        assertThat(status.items()).hasSize(1);
        assertThat(status.items().getFirst().processing()).isEqualTo(2);
        assertThat(status.items().getFirst().scheduled()).isZero();
    }

    @Test
    void shouldExposeScheduledInStatusForRelaxedDispatcher() {
        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        final TaskStatus status = dispatcher.getStatus();

        assertThat(status.percentageCompleted()).isZero();
        assertThat(status.items()).hasSize(1);
        assertThat(status.items().getFirst().scheduled()).isEqualTo(2);
        assertThat(status.items().getFirst().processing()).isZero();
    }

    @Test
    void shouldTransitionDispatcherStateWhenCancelledWithActiveSubTask() {
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern firstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, firstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        rootProvider.setNetwork(network);
        rootProvider.setActive(true);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(firstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        dispatcher.cancel();
        rootProvider.doWork();

        assertThat(dispatcher.getState()).isEqualTo(TaskState.RETURNING_INTERNAL_STORAGE);
    }

    @Test
    void shouldReturnEmptyWhenOutcomeHasNoExecutableResult() {
        final LpCraftingSolver.PlanningOutcome outcome = new LpCraftingSolver.PlanningOutcome(
            0,
            Optional.empty(),
            new LpResourceSet(),
            Set.of()
        );

        final Optional<com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan> taskPlan =
            invokeToTaskPlan(B, 1, outcome);

        assertThat(taskPlan).isEmpty();
    }

    @Test
    void shouldApplyDispatcherStaticUtilityMethods() {
        final Pattern stepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final LpExecutionPlanStep step = new LpExecutionPlanStep(LpPatternRecipe.fromPattern(stepPattern, 0), 5);
        final Map<ResourceKey, Long> available = new HashMap<>();
        available.put(A, 8L);

        final long maxIterations = invokeDispatcherMaxDispatchableIterations(step, available);
        final Map<ResourceKey, Long> requirements = invokeDispatcherStepRequirements(step, 2);
        final boolean canFulfillBefore = invokeDispatcherCanFulfill(requirements, available);
        invokeDispatcherConsume(requirements, available);
        final boolean canFulfillAfter = invokeDispatcherCanFulfill(requirements, available);

        assertThat(maxIterations).isEqualTo(4);
        assertThat(requirements).containsEntry(A, 4L);
        assertThat(canFulfillBefore).isTrue();
        assertThat(available).containsEntry(A, 4L);
        assertThat(canFulfillAfter).isTrue();
    }

    @Test
    void shouldStepDispatcherFromReadyToRunningThenReportNoChange() {
        final Pattern blockedFirstStepPattern = pattern().ingredient(A, 2).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(C, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, blockedFirstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(blockedFirstStepPattern, 0), 2),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );

        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, false);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        final boolean firstChanged = dispatcher.step(
            rootStorage,
            layout -> List.of(),
            com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior.DEFAULT,
            com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener.EMPTY
        );
        final boolean secondChanged = dispatcher.step(
            rootStorage,
            layout -> List.of(),
            com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior.DEFAULT,
            com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener.EMPTY
        );

        assertThat(firstChanged).isTrue();
        assertThat(secondChanged).isFalse();
        assertThat(dispatcher.getState()).isEqualTo(TaskState.RUNNING);
    }

    @Test
    void shouldInterceptViaActiveSubTaskInBeforeAndAfterInsert() {
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);

        final Pattern firstStepPattern = pattern().ingredient(C, 1).output(B, 1).build();
        final Pattern rootPattern = pattern().ingredient(B, 1).output(D, 1).build();

        final PatternProviderNetworkNode firstProvider = new PatternProviderNetworkNode(0, 5);
        firstProvider.setPattern(1, firstStepPattern);
        sut.onContainerAdded(() -> firstProvider);

        final PatternProviderNetworkNode rootProvider = new PatternProviderNetworkNode(0, 5);
        rootProvider.setPattern(1, rootPattern);
        sut.onContainerAdded(() -> rootProvider);

        final List<LpExecutionPlanStep> steps = List.of(
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(firstStepPattern, 0), 1),
            new LpExecutionPlanStep(LpPatternRecipe.fromPattern(rootPattern, 1), 1)
        );
        final Optional<TaskId> dispatcherTask = invokeAddLpDispatcherTask(D, 1, steps, true);
        assertThat(dispatcherTask).isPresent();
        final var dispatcher = rootProvider.getTasks().getFirst();

        invokeDispatcherBooleanMethod(dispatcher, "dispatchStrict", rootStorage);

        final long beforeIntercepted = dispatcher.beforeInsert(C, 1);
        final long afterIntercepted = dispatcher.afterInsert(C, 1);

        assertThat(beforeIntercepted).isBetween(0L, 1L);
        assertThat(afterIntercepted).isBetween(0L, 1L);
    }

    @Test
    void shouldNotReadTaskStatusWhenNoListenersAreRegistered() {
        final com.refinedmods.refinedstorage.api.autocrafting.task.Task throwingTask =
            new com.refinedmods.refinedstorage.api.autocrafting.task.Task() {
                @Override
                public Actor getActor() {
                    return Actor.EMPTY;
                }

                @Override
                public boolean shouldNotify() {
                    return false;
                }

                @Override
                public ResourceKey getResource() {
                    return B;
                }

                @Override
                public long getAmount() {
                    return 1;
                }

                @Override
                public TaskId getId() {
                    return TaskId.create();
                }

                @Override
                public TaskState getState() {
                    return TaskState.READY;
                }

                @Override
                public boolean step(final RootStorage rootStorage,
                                    final com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider sinkProvider,
                                    final com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior stepBehavior,
                                    final com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener listener) {
                    return false;
                }

                @Override
                public void cancel() {
                }

                @Override
                public TaskStatus getStatus() {
                    throw new AssertionError("Status should not be requested when there are no listeners");
                }

                @Override
                public long beforeInsert(final ResourceKey insertedResource, final long insertedAmount) {
                    return 0;
                }

                @Override
                public long afterInsert(final ResourceKey insertedResource, final long insertedAmount) {
                    return 0;
                }

                @Override
                public void changed(final com.refinedmods.refinedstorage.api.resource.list.MutableResourceList.OperationResult change) {
                }
            };

        sut.taskChanged(throwingTask);
    }

    @Test
    void shouldReadTaskStatusWhenListenerIsRegistered() {
        final boolean[] changed = {false};
        sut.addListener(new TaskStatusListener() {
            @Override
            public void taskAdded(final TaskStatus taskStatus) {
            }

            @Override
            public void taskRemoved(final TaskId taskId) {
            }

            @Override
            public void taskStatusChanged(final TaskStatus status) {
                changed[0] = true;
            }
        });

        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );
        assertThat(taskId).isPresent();

        final var task = provider.getTasks().getFirst();
        sut.taskChanged(task);

        assertThat(changed[0]).isTrue();
    }

    @Test
    void shouldInvokePrivateShouldUseLpSystemWithRootStorageProvider() {
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        assertThat(invokeShouldUseLpSystemViaPrivateMethod(B)).isTrue();
    }

    @Test
    void shouldExposePatternsAndPatternsByOutput() {
        final Pattern patternAB = pattern().ingredient(A, 1).output(B, 1).build();
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, patternAB);
        sut.onContainerAdded(() -> provider);

        assertThat(sut.getPatterns()).contains(patternAB);
        assertThat(sut.getPatternsByOutput(B)).contains(patternAB);
    }

    @Test
    void shouldNotifyPatternListenersOnAddAndRemove() {
        final List<Pattern> added = new ArrayList<>();
        final List<Pattern> removed = new ArrayList<>();
        sut.addListener(new com.refinedmods.refinedstorage.api.network.autocrafting.PatternListener() {
            @Override
            public void onAdded(final Pattern pattern) {
                added.add(pattern);
            }

            @Override
            public void onRemoved(final Pattern pattern) {
                removed.add(pattern);
            }
        });

        final Pattern patternAB = pattern().ingredient(A, 1).output(B, 1).build();
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, patternAB);

        sut.onContainerAdded(() -> provider);
        sut.onContainerRemoved(() -> provider);

        assertThat(added).contains(patternAB);
        assertThat(removed).contains(patternAB);
    }

    @Test
    void shouldNotifyTaskStatusListenersOnTaskAddedAndRemoved() {
        final List<TaskStatus> added = new ArrayList<>();
        final List<TaskId> removed = new ArrayList<>();
        sut.addListener(new TaskStatusListener() {
            @Override
            public void taskAdded(final TaskStatus taskStatus) {
                added.add(taskStatus);
            }

            @Override
            public void taskRemoved(final TaskId taskId) {
                removed.add(taskId);
            }

            @Override
            public void taskStatusChanged(final TaskStatus status) {
            }
        });

        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        final Optional<TaskId> taskId = startTaskExpectingAlgorithm(
            B,
            1,
            Actor.EMPTY,
            false,
            CancellationToken.NONE,
            PlanningAlgorithm.LP
        );

        assertThat(taskId).isPresent();
        assertThat(added).isNotEmpty();

        sut.cancel(taskId.get());
        provider.setActive(true);
        provider.setNetwork(network);
        provider.doWork();
        provider.doWork();

        assertThat(removed).contains(taskId.get());
    }

    @Test
    void shouldUpdatePatternPriorityOrdering() {
        final Pattern lowPriority = pattern().ingredient(A, 1).output(B, 1).build();
        final Pattern highPriority = pattern().ingredient(C, 1).output(B, 1).build();
        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.setPattern(1, lowPriority);
        provider.setPattern(2, highPriority);
        sut.onContainerAdded(() -> provider);

        sut.update(highPriority, 100);

        assertThat(sut.getPatternsByOutput(B).getFirst()).isEqualTo(highPriority);
    }

    private Optional<TaskId> startTaskExpectingAlgorithm(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify,
                                                         final CancellationToken cancellationToken,
                                                         final PlanningAlgorithm expectedAlgorithm) {
        assertThat(determinePlanningAlgorithm(resource)).isEqualTo(expectedAlgorithm);
        return sut.startTask(resource, amount, actor, notify, cancellationToken);
    }

    private AutocraftingNetworkComponent.EnsureResult ensureTaskExpectingAlgorithm(
        final ResourceKey resource,
        final long amount,
        final Actor actor,
        final CancellationToken cancellationToken,
        final PlanningAlgorithm expectedAlgorithm
    ) {
        assertThat(determinePlanningAlgorithm(resource)).isEqualTo(expectedAlgorithm);
        return sut.ensureTask(resource, amount, actor, cancellationToken);
    }

    private PlanningAlgorithm determinePlanningAlgorithm(final ResourceKey resource) {
        return invokeShouldUseLpSystem(resource) ? PlanningAlgorithm.LP : PlanningAlgorithm.TRADITIONAL;
    }

    private boolean invokeShouldUseLpSystem(final ResourceKey resource) {
        try {
            final var field = AutocraftingNetworkComponentImpl.class.getDeclaredField("patternRepository");
            field.setAccessible(true);
            final var patternRepository = field.get(sut);
            return LpPlanningHelper.shouldUseLPSystem(resource, rootStorage, (com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl) patternRepository);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to determine selected planning algorithm", e);
        }
    }

    private boolean invokeShouldUseLpSystemViaPrivateMethod(final ResourceKey resource) {
        try {
            final Method method = AutocraftingNetworkComponentImpl.class.getDeclaredMethod(
                "shouldUseLPSystem",
                ResourceKey.class
            );
            method.setAccessible(true);
            return (boolean) method.invoke(sut, resource);
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke shouldUseLPSystem(ResourceKey)", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<TaskId> invokeAddLpDispatcherTask(final ResourceKey resource,
                                                       final long amount,
                                                       final List<LpExecutionPlanStep> steps,
                                                       final boolean hasRecipeCycles) {
        return invokeAddLpDispatcherTask(resource, amount, steps, hasRecipeCycles, false);
    }

    @SuppressWarnings("unchecked")
    private Optional<TaskId> invokeAddLpDispatcherTask(final ResourceKey resource,
                                                       final long amount,
                                                       final List<LpExecutionPlanStep> steps,
                                                       final boolean hasRecipeCycles,
                                                       final boolean notify) {
        try {
            final LpStepPlan lpStepPlan = new LpStepPlan(steps, hasRecipeCycles);
            final Method method = AutocraftingNetworkComponentImpl.class.getDeclaredMethod(
                "addLpDispatcherTask",
                ResourceKey.class,
                long.class,
                Actor.class,
                LpStepPlan.class,
                boolean.class
            );
            method.setAccessible(true);
            return (Optional<TaskId>) method.invoke(sut, resource, amount, Actor.EMPTY, lpStepPlan, notify);
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke LP dispatcher task creation", e);
        }
    }

    private com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan invokeToSingleStepPlan(
        final ResourceKey requestedResource,
        final long requestedAmount,
        final LpExecutionPlanStep step,
        final boolean root
    ) {
        return LpDispatcherHelper.toSingleStepPlan(requestedResource, requestedAmount, step, root);
    }

    private boolean invokeDispatcherBooleanMethod(final com.refinedmods.refinedstorage.api.autocrafting.task.Task task,
                                                  final String methodName,
                                                  final RootStorage rootStorage) {
        try {
            final Method method = task.getClass().getDeclaredMethod(methodName, RootStorage.class);
            method.setAccessible(true);
            return (boolean) method.invoke(task, rootStorage);
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke dispatcher method " + methodName, e);
        }
    }

    private Pattern invokeFindRootPattern(final ResourceKey resource,
                                          final List<LpExecutionPlanStep> steps) {
        return LpDispatcherHelper.findRootPattern(resource, steps);
    }

    private long invokeDispatcherPendingRequirement(
        final com.refinedmods.refinedstorage.api.autocrafting.task.Task task,
        final ResourceKey resource
    ) {
        try {
            final Method method = task.getClass().getDeclaredMethod("getPendingRequirement", ResourceKey.class);
            method.setAccessible(true);
            return (long) method.invoke(task, resource);
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke dispatcher getPendingRequirement", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Pattern> invokeCollectRelevantPatternsForLp(final ResourceKey resource,
                                                                    final RootStorage rootStorage) {
        try {
            final var field = AutocraftingNetworkComponentImpl.class.getDeclaredField("patternRepository");
            field.setAccessible(true);
            final var patternRepository = field.get(sut);
            return LpPlanningHelper.collectRelevantPatternsForLp(resource, rootStorage, (com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl) patternRepository);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to invoke collectRelevantPatternsForLp", e);
        }
    }

    private long invokeFindMaxCraftableAmountViaLp(final Collection<Pattern> relevantPatterns,
                                                   final RootStorage rootStorage,
                                                   final ResourceKey resource,
                                                   final long amount,
                                                   final CancellationToken cancellationToken) {
        try {
            final Method method = AutocraftingNetworkComponentImpl.class.getDeclaredMethod(
                "findMaxCraftableAmountViaLp",
                Collection.class,
                RootStorage.class,
                ResourceKey.class,
                long.class,
                CancellationToken.class
            );
            method.setAccessible(true);
            return (long) method.invoke(sut, relevantPatterns, rootStorage, resource, amount, cancellationToken);
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke findMaxCraftableAmountViaLp", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan> invokeToTaskPlan(
        final ResourceKey resource,
        final long amount,
        final LpCraftingSolver.PlanningOutcome outcome
    ) {
        try {
            final Method method = AutocraftingNetworkComponentImpl.class.getDeclaredMethod(
                "toTaskPlan",
                ResourceKey.class,
                long.class,
                LpCraftingSolver.PlanningOutcome.class
            );
            method.setAccessible(true);
            return (Optional<com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan>) method.invoke(
                sut,
                resource,
                amount,
                outcome
            );
        } catch (final NoSuchMethodException
                     | IllegalAccessException
                     | InvocationTargetException e) {
            throw new AssertionError("Unable to invoke toTaskPlan", e);
        }
    }

    private long invokeDispatcherMaxDispatchableIterations(final LpExecutionPlanStep step,
                                                           final Map<ResourceKey, Long> available) {
        try {
            final Class<?> dispatcherClass = Class.forName(
                "com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl$LpStepDispatcher"
            );
            final Method method = dispatcherClass.getDeclaredMethod(
                "maxDispatchableIterations",
                LpExecutionPlanStep.class,
                Map.class
            );
            method.setAccessible(true);
            return (long) method.invoke(null, step, available);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Unable to invoke maxDispatchableIterations", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<ResourceKey, Long> invokeDispatcherStepRequirements(final LpExecutionPlanStep step,
                                                                    final long iterations) {
        try {
            final Class<?> dispatcherClass = Class.forName(
                "com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl$LpStepDispatcher"
            );
            final Method method = dispatcherClass.getDeclaredMethod(
                "stepRequirements",
                LpExecutionPlanStep.class,
                long.class
            );
            method.setAccessible(true);
            return (Map<ResourceKey, Long>) method.invoke(null, step, iterations);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Unable to invoke stepRequirements", e);
        }
    }

    private boolean invokeDispatcherCanFulfill(final Map<ResourceKey, Long> requirements,
                                               final Map<ResourceKey, Long> available) {
        try {
            final Class<?> dispatcherClass = Class.forName(
                "com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl$LpStepDispatcher"
            );
            final Method method = dispatcherClass.getDeclaredMethod("canFulfill", Map.class, Map.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, requirements, available);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Unable to invoke canFulfill", e);
        }
    }

    private void invokeDispatcherConsume(final Map<ResourceKey, Long> requirements,
                                         final Map<ResourceKey, Long> available) {
        try {
            final Class<?> dispatcherClass = Class.forName(
                "com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl$LpStepDispatcher"
            );
            final Method method = dispatcherClass.getDeclaredMethod("consume", Map.class, Map.class);
            method.setAccessible(true);
            method.invoke(null, requirements, available);
        } catch (final ReflectiveOperationException e) {
            throw new AssertionError("Unable to invoke consume", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void clearDispatcherPendingSteps(final com.refinedmods.refinedstorage.api.autocrafting.task.Task task) {
        try {
            final var field = task.getClass().getDeclaredField("pendingSteps");
            field.setAccessible(true);
            ((List<LpExecutionPlanStep>) field.get(task)).clear();
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to clear dispatcher pendingSteps", e);
        }
    }

    private enum PlanningAlgorithm {
        TRADITIONAL,
        LP
    }

    private static class CancelledCancellationToken implements CancellationToken {
        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public void cancel() {
            // no op
        }
    }
}
