package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskImpl;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NetworkTest
@SetupNetwork
class PatternProviderNetworkNodeTest {
    private static final Pattern PATTERN_A = pattern().output(A, 1).ingredient(C, 1).build();
    private static final Pattern PATTERN_B = pattern().output(B, 1).ingredient(C, 1).build();

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
    })
    private PatternProviderNetworkNode sut;

    @Test
    void testDefaultState(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldSetPatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Act
        sut.setPattern(0, PATTERN_A);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldRemovePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setPattern(0, null);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldReplacePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setPattern(0, PATTERN_B);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(B);
    }

    @Test
    void shouldRemovePatternsFromNetworkWhenInactive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldAddPatternsFromNetworkWhenActive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);
        sut.setActive(false);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldNotStepTasksWithoutNetwork(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act
        sut.setNetwork(null);
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
    }

    @Test
    void shouldNotStepTasksWhenInactive(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act
        sut.setActive(false);
        sut.doWork();

        // Assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
    }

    @Test
    void shouldStepTasks(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1)
        );
        assertThat(sut.getTasks()).isEmpty();
    }

    @Test
    void shouldStepTasksWithCustomStepBehavior(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 60, Action.EXECUTE, Actor.EMPTY);

        sut.setStepBehavior(new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 10;
            }
        });

        sut.setPattern(1, pattern().ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 20, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 60)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).isEmpty();
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(B, 20)
        );
        assertThat(sut.getTasks()).isEmpty();
    }

    @Test
    void shouldUseProviderAsSinkForExternalPatternInputsWhenSinkIsAttached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        sut.setSink(sink);
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sink.getAll()).isEmpty();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sink.getAll()).isEmpty();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
    }

    @Test
    void shouldUseProviderAsSinkForExternalPatternInputsWhenSinkIsAttachedAndDoesNotAcceptResources(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        sut.setSink((resources, action) -> ExternalPatternSink.Result.REJECTED);
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
    }

    @Test
    void shouldNotUseProviderAsSinkForExternalPatternInputsWhenThereIsNoSinkAttached(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 1).build());
        assertThat(autocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        // Act & assert
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 10)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );

        sut.doWork();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7)
        );
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 3)
        );
    }

    @Test
    void shouldReserveNetworkInsertionsWhenWaitingForExternalPatternRootOutputs(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final PatternProviderListener listener = mock(PatternProviderListener.class);

        storage.addSource(new StorageImpl());
        storage.insert(A, 6, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 3).output(B, 5).build());
        // swallow resources
        sut.setSink((resources, action) -> ExternalPatternSink.Result.ACCEPTED);
        sut.setListener(listener);

        // Act & assert
        assertThat(autocrafting.startTask(B, 10, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 6));
        assertThat(storage.getAll()).isEmpty();
        verify(listener, never()).receivedExternalIteration();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 3));
        assertThat(storage.getAll()).isEmpty();
        verify(listener, never()).receivedExternalIteration();

        storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 3));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 3)
        );
        verify(listener, never()).receivedExternalIteration();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 3)
        );
        verify(listener, never()).receivedExternalIteration();

        storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 6)
        );
        verify(listener, never()).receivedExternalIteration();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 6)
        );
        verify(listener, times(1)).receivedExternalIteration();
        clearInvocations(listener);

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 6)
        );
        verify(listener, never()).receivedExternalIteration();

        storage.insert(B, 5, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 11)
        );
        verify(listener, never()).receivedExternalIteration();

        sut.doWork();
        assertThat(sut.getTasks()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 11)
        );
        verify(listener, times(1)).receivedExternalIteration();
        clearInvocations(listener);
    }

    @Test
    void shouldNotReserveNetworkInsertionsWhenWaitingForExternalPatternRootOutputsAndNetworkDoesNotAcceptResource(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl() {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (resource == A) {
                    return 0;
                }
                return super.insert(resource, amount, action, actor);
            }
        });
        storage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(B, 1).output(A, 1).build());
        // swallow resources
        sut.setSink((resources, action) -> ExternalPatternSink.Result.ACCEPTED);

        // Act & assert
        assertThat(autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(B, 1));
        assertThat(storage.getAll()).isEmpty();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).isEmpty();

        storage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).isEmpty();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).isEmpty();
    }

    @Test
    void shouldInterceptNetworkInsertionsWhenWaitingForExternalPatternIntermediateOutputs(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1).build());
        sut.setPattern(2, pattern().ingredient(B, 1).output(C, 1).build());
        // swallow resources
        sut.setSink((resources, action) -> ExternalPatternSink.Result.ACCEPTED);

        // Act & assert
        assertThat(autocrafting.startTask(C, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 3));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 2));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(A, 1));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        storage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(B, 1));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 7)
        );

        storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(new ResourceAmount(B, 3));
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1),
            new ResourceAmount(C, 1)
        );

        sut.doWork();
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1),
            new ResourceAmount(C, 2)
        );

        sut.doWork();
        assertThat(sut.getTasks()).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 7),
            new ResourceAmount(B, 1),
            new ResourceAmount(C, 3)
        );
    }

    @Test
    void shouldNotifyStatusListenerWhenTaskIsAdded(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final TaskStatusListener listener = mock(TaskStatusListener.class);
        autocrafting.addListener(listener);

        storage.addSource(new StorageImpl());
        storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, PATTERN_A);

        // Act
        final var taskId = autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join();

        // Assert
        assertThat(taskId).isPresent();
        final ArgumentCaptor<TaskStatus> statusCaptor = ArgumentCaptor.forClass(TaskStatus.class);
        verify(listener, times(1)).taskAdded(statusCaptor.capture());
        final TaskStatus status = statusCaptor.getValue();
        assertThat(status.info().id()).isEqualTo(taskId.get());
    }

    @Test
    void shouldNotNotifyStatusListenerWhenTaskIsNotAddedDueToMissingResources(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final TaskStatusListener listener = mock(TaskStatusListener.class);
        autocrafting.addListener(listener);

        sut.setPattern(1, PATTERN_A);

        // Act
        final var taskId = autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join();

        // Assert
        assertThat(taskId).isEmpty();
        verify(listener, never()).taskAdded(any());
    }

    @Test
    void shouldNotifyStatusListenerWhenTaskIsCompleted(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final TaskStatusListener listener = mock(TaskStatusListener.class);
        autocrafting.addListener(listener);

        storage.addSource(new StorageImpl());
        storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setActive(true);
        sut.setPattern(1, PATTERN_A);

        // Act & assert
        final Optional<TaskId> createdId =
            autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join();
        assertThat(createdId).isPresent();
        assertThat(sut.getTasks()).isNotEmpty();

        sut.doWork();
        sut.doWork();
        assertThat(sut.getTasks()).isEmpty();

        final ArgumentCaptor<TaskId> idCaptor = ArgumentCaptor.forClass(TaskId.class);
        verify(listener, times(1)).taskRemoved(idCaptor.capture());
        final TaskId id = idCaptor.getValue();
        assertThat(createdId).contains(id);
    }

    @Test
    void shouldNotifyStatusListenerWhenTaskHasChanged(
        @InjectNetworkStorageComponent final StorageNetworkComponent storage,
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final TaskStatusListener listener = mock(TaskStatusListener.class);
        autocrafting.addListener(listener);

        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        sut.setPattern(1, pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1).build());
        // swallow resources
        sut.setSink((resources, action) -> ExternalPatternSink.Result.ACCEPTED);

        // Act & assert
        assertThat(autocrafting.startTask(B, 2, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

        sut.doWork();
        taskShouldBeMarkedAsChangedOnce(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 2)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8)
        );

        sut.doWork();
        taskShouldBeMarkedAsChangedOnce(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactlyInAnyOrder(
            new ResourceAmount(A, 1)
        );
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8)
        );

        sut.doWork();
        taskShouldBeMarkedAsChangedOnce(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8)
        );

        sut.doWork();
        taskShouldNotBeMarkedAsChanged(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8)
        );

        storage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        taskShouldNotBeMarkedAsChanged(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8),
            new ResourceAmount(B, 1)
        );

        sut.doWork();
        taskShouldBeMarkedAsChangedOnce(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8),
            new ResourceAmount(B, 1)
        );

        sut.doWork();
        taskShouldNotBeMarkedAsChanged(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8),
            new ResourceAmount(B, 1)
        );

        storage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
        taskShouldNotBeMarkedAsChanged(listener);
        assertThat(sut.getTasks()).hasSize(1);
        assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
        assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 8),
            new ResourceAmount(B, 2)
        );

        sut.doWork();
        taskShouldNotBeMarkedAsChanged(listener);
        verify(listener, times(1)).taskRemoved(any());
        assertThat(sut.getTasks()).isEmpty();
    }

    private static void taskShouldBeMarkedAsChangedOnce(final TaskStatusListener listener) {
        verify(listener, times(1)).taskStatusChanged(any());
        reset(listener);
    }

    private static void taskShouldNotBeMarkedAsChanged(final TaskStatusListener listener) {
        verify(listener, never()).taskStatusChanged(any());
    }

    private static Collection<ResourceAmount> copyInternalStorage(final Task task) {
        return ((TaskImpl) task).createSnapshot().copyInternalStorage().copyState();
    }

    @Nested
    class ExternalBalancingTest {
        @AddNetworkNode(properties = {
            @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
        })
        private PatternProviderNetworkNode sut2;

        @AddNetworkNode(properties = {
            @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
        })
        private PatternProviderNetworkNode sut3;

        @Test
        void shouldBalanceExternalInputsOverMultipleSinks(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternBuilder patternBuilder = pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1);

            sut.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
            sut.setSink(sink);

            sut2.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink2 = new PatternProviderExternalPatternSinkImpl();
            sut2.setSink(sink2);

            assertThat(autocrafting.startTask(B, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut2.getTasks()).isEmpty();

            // Act & assert
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).isEmpty();
            assertThat(sink2.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(sink2.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new ResourceAmount(A, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(sink2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(sink2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
        }

        @Test
        void shouldUseNextAvailableSinkIfItIsNotAcceptingResources(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternBuilder patternBuilder = pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1);

            sut.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
            sut.setSink(sink);

            sut2.setPattern(1, patternBuilder.build());
            sut2.setSink((resources, action) -> ExternalPatternSink.Result.REJECTED);

            sut3.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink3 = new PatternProviderExternalPatternSinkImpl();
            sut3.setSink(sink3);

            assertThat(autocrafting.startTask(B, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut2.getTasks()).isEmpty();
            assertThat(sut3.getTasks()).isEmpty();

            // Act & assert
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).isEmpty();
            assertThat(sink3.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(sink3.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new ResourceAmount(A, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(sink3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(sink3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
        }

        @Test
        void shouldDoNothingIfNoSinkAcceptsResources(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternBuilder patternBuilder = pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1);

            sut.setPattern(1, patternBuilder.build());
            sut.setSink((resources, action) -> ExternalPatternSink.Result.REJECTED);

            sut2.setPattern(1, patternBuilder.build());
            sut2.setSink((resources, action) -> ExternalPatternSink.Result.REJECTED);

            assertThat(autocrafting.startTask(B, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut2.getTasks()).isEmpty();

            // Act & assert
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
        }

        @Test
        void shouldNotWrapAroundIfLastSinkIsNotAcceptingResources(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternBuilder patternBuilder = pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1);

            sut.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
            sut.setSink(sink);

            sut2.setPattern(1, patternBuilder.build());
            sut2.setSink((resources, action) -> ExternalPatternSink.Result.REJECTED);

            assertThat(autocrafting.startTask(B, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(sut2.getTasks()).isEmpty();

            // Act & assert
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 1)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 2)
            );
        }

        @Test
        void shouldDoNothingIfThereAreNoSinksAnymore(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternBuilder patternBuilder = pattern(PatternType.EXTERNAL).ingredient(A, 1).output(B, 1);

            sut.setPattern(1, patternBuilder.build());
            final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
            sut.setSink(sink);

            assertThat(autocrafting.startTask(B, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);

            // Act & assert
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).isEmpty();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            sut.setPattern(1, null);
            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(A, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 7)
            );
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );
        }
    }

    @Nested
    @SetupNetwork(id = "other")
    class NetworkChangeTest {
        @Test
        void shouldInterceptInsertionsOnNewNetworkWhenNetworkChanges(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting,
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetworkStorageComponent(networkId = "other") final StorageNetworkComponent otherStorage
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            otherStorage.addSource(new StorageImpl());

            sut.setPattern(1, pattern(PatternType.EXTERNAL)
                .ingredient(B, 1)
                .output(A, 1)
                .build());
            sut.setPattern(2, pattern(PatternType.EXTERNAL)
                .ingredient(C, 1)
                .output(B, 1)
                .build());
            // swallow resources
            sut.setSink((resources, action) -> ExternalPatternSink.Result.ACCEPTED);

            // Act & assert
            assertThat(autocrafting.startTask(A, 3, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(C, 3)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(C, 7)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(C, 2)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 7)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).containsExactly(
                new ResourceAmount(C, 1)
            );
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 7)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 7)
            );

            sut.setNetwork(otherNetwork);
            otherNetwork.addContainer(() -> sut);

            storage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).isEmpty();

            otherStorage.insert(B, 3, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new ResourceAmount(B, 3));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).isEmpty();

            sut.doWork();
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new ResourceAmount(B, 2));

            sut.doWork();
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new ResourceAmount(B, 1));

            sut.doWork();
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();

            storage.insert(A, 3, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(C, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).isEmpty();

            otherStorage.insert(A, 3, Action.EXECUTE, Actor.EMPTY);
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(A, 3),
                new ResourceAmount(C, 7),
                new ResourceAmount(B, 3)
            );
            assertThat(otherStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 3)
            );

            sut.doWork();
            assertThat(sut.getTasks()).isEmpty();
            assertThat(otherStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 3)
            );
        }

        @Test
        void shouldNotifyStatusListenersOfOldAndNewNetworkWhenNetworkChanges(
            @InjectNetwork final Network network,
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting,
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetworkAutocraftingComponent(networkId = "other")
            final AutocraftingNetworkComponent otherAutocrafting
        ) {
            // Arrange
            final TaskStatusListener listener = mock(TaskStatusListener.class);
            autocrafting.addListener(listener);

            final TaskStatusListener otherListener = mock(TaskStatusListener.class);
            otherAutocrafting.addListener(otherListener);

            storage.addSource(new StorageImpl());
            storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            sut.setPattern(1, PATTERN_A);

            // Act & assert
            final var taskId = autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join();
            assertThat(taskId).isPresent();
            final ArgumentCaptor<TaskStatus> statusCaptor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(listener, times(1)).taskAdded(statusCaptor.capture());
            verify(listener, never()).taskRemoved(any());
            verify(otherListener, never()).taskAdded(any());
            verify(otherListener, never()).taskRemoved(any());
            final TaskStatus status = statusCaptor.getValue();
            assertThat(status.info().id()).isEqualTo(taskId.get());

            reset(listener, otherListener);

            network.removeContainer(() -> sut);
            sut.setNetwork(otherNetwork);
            otherNetwork.addContainer(() -> sut);

            verify(listener, never()).taskAdded(any());
            final ArgumentCaptor<TaskId> removedIdCaptor = ArgumentCaptor.forClass(TaskId.class);
            verify(listener, times(1)).taskRemoved(removedIdCaptor.capture());
            final ArgumentCaptor<TaskStatus> addedTaskCaptor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(otherListener, times(1)).taskAdded(addedTaskCaptor.capture());
            verify(otherListener, never()).taskRemoved(any());
        }

        @Test
        void shouldBeAbleToCancelTaskInNewNetworkWhenNetworkChanges(
            @InjectNetwork final Network network,
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting,
            @InjectNetwork("other") final Network otherNetwork,
            @InjectNetworkAutocraftingComponent(networkId = "other")
            final AutocraftingNetworkComponent otherAutocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            sut.setPattern(1, PATTERN_A);

            // Act & assert
            final var taskId = autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join();
            assertThat(taskId).isPresent();
            assertThat(sut.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

            network.removeContainer(() -> sut);
            sut.setNetwork(otherNetwork);
            otherNetwork.addContainer(() -> sut);

            autocrafting.cancel(taskId.get());
            assertThat(sut.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

            otherAutocrafting.cancel(taskId.get());
            assertThat(sut.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        }
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode(properties = {
            @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
        })
        private PatternProviderNetworkNode other;

        @Test
        void shouldNotUseProviderAsSinkForExternalChildPatternWhenProviderIsRemovedAndRootProviderIsStillPresent(
            @InjectNetworkStorageComponent final StorageNetworkComponent storage,
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            storage.addSource(new StorageImpl());
            storage.insert(C, 10, Action.EXECUTE, Actor.EMPTY);

            final Pattern patternForA = pattern().output(A, 1).ingredient(B, 1).build();
            sut.setPattern(0, patternForA);

            final Pattern patternForB = pattern(PatternType.EXTERNAL)
                .output(B, 1)
                .ingredient(C, 1)
                .build();
            other.setPattern(0, patternForB);

            // Act & assert
            assertThat(autocrafting.startTask(A, 1, Actor.EMPTY, false, CancellationToken.NONE).join()).isPresent();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst())).isEmpty();
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 10)
            );

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(C, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 9)
            );

            autocrafting.onContainerRemoved(() -> other);

            sut.doWork();
            assertThat(sut.getTasks()).hasSize(1);
            assertThat(copyInternalStorage(sut.getTasks().getFirst()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ResourceAmount(C, 1));
            assertThat(storage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
                new ResourceAmount(C, 9)
            );
        }

        @Test
        void shouldSetPatternsRespectingPriority(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            // Act
            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldRemovePatternsRespectingPriorityWhenInactive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            other.setActive(false);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactly(A);
            assertThat(autocrafting.getPatternsByOutput(A)).containsExactly(patternWithLowestPriority);
        }

        @Test
        void shouldAddPatternsRespectingPriorityWhenActive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            sut.setActive(false);
            other.setActive(false);

            // Act
            sut.setActive(true);
            other.setActive(true);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldModifyPriorityAfterAddingPatterns(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            sut.setPriority(2);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithLowestPriority, patternWithHighestPriority);
        }
    }
}
