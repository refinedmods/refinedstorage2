package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewItem;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.NetworkImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.network.test.fixtures.NetworkTestFixtures;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
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
        provider1.tryUpdatePattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.tryUpdatePattern(1, pattern2);
        provider2.tryUpdatePattern(2, pattern3);

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
        provider1.tryUpdatePattern(1, pattern1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        final Pattern pattern2 = patternABuilder.build();
        final Pattern pattern3 = patternBBuilder.build();
        provider2.tryUpdatePattern(1, pattern2);
        provider2.tryUpdatePattern(2, pattern3);

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 4).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 4).output(B, 1).build());
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);

        // Assert
        assertThat(taskId).isPresent();
        assertThat(provider.getTasks()).hasSize(1);
    }

    @Test
    void shouldNotStartTaskIfCancelled() {
        // Arrange
        rootStorage.addSource(new StorageImpl());
        rootStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderNetworkNode provider = new PatternProviderNetworkNode(0, 5);
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = sut.startTask(B, 1, Actor.EMPTY, false, new CancelledCancellationToken());

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE)).isPresent();
        final var result = sut.ensureTask(B, 10, Actor.EMPTY, CancellationToken.NONE);
        assertThat(result).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_CREATED);
        final var result2 = sut.ensureTask(B, 10, Actor.EMPTY, CancellationToken.NONE);
        assertThat(result2).isEqualTo(AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING);
        final var result3 = sut.ensureTask(B, 9, Actor.EMPTY, CancellationToken.NONE);
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act & assert
        assertThat(sut.startTask(B, 1, Actor.EMPTY, false, new CancelledCancellationToken())).isEmpty();
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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = sut.ensureTask(B, 1, Actor.EMPTY, CancellationToken.NONE);

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = sut.ensureTask(B, 11, Actor.EMPTY, CancellationToken.NONE);

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 1).output(B, 1).build());
        sut.onContainerAdded(() -> provider);

        // Act
        final var result = sut.ensureTask(B, 11, Actor.EMPTY, CancellationToken.NONE);

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);

        // Act
        final var result = sut.ensureTask(B, 1, Actor.EMPTY, CancellationToken.NONE);

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
        provider1.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.tryUpdatePattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false, CancellationToken.NONE);

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
        provider1.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.setActive(true);
        provider2.setNetwork(network);
        provider2.tryUpdatePattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false, CancellationToken.NONE);

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
        provider.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        final NetworkNodeContainer container = () -> provider;
        sut.onContainerAdded(container);

        // Act
        final Optional<TaskId> taskId = sut.startTask(B, 2, Actor.EMPTY, false, CancellationToken.NONE);

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
        provider1.tryUpdatePattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        sut.onContainerAdded(() -> provider1);

        final PatternProviderNetworkNode provider2 = new PatternProviderNetworkNode(0, 5);
        provider2.tryUpdatePattern(1, pattern().ingredient(A, 3).output(C, 1).build());
        sut.onContainerAdded(() -> provider2);

        final PatternProviderNetworkNode provider3 = new PatternProviderNetworkNode(0, 5);
        provider3.tryUpdatePattern(1, pattern().ingredient(A, 3).output(D, 1).build());
        sut.onContainerAdded(() -> provider3);

        final Optional<TaskId> taskId1 = sut.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        final Optional<TaskId> taskId2 = sut.startTask(C, 1, Actor.EMPTY, false, CancellationToken.NONE);

        sut.startTask(D, 1, Actor.EMPTY, false, CancellationToken.NONE);
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
