package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderExternalPatternSinkImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderListener;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addPattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayAutocraftingNetworkNodeTest {
    @SuppressWarnings("DefaultAnnotationParam")
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void shouldPassAutocraftingComponent(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        final var removeB = addPattern(inputAutocrafting, B);
        removeB.run();

        addPattern(inputAutocrafting, C);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).containsExactlyInAnyOrder(A, C);
        assertThat(outputAutocrafting.getOutputs()).containsExactlyInAnyOrder(A, C);
        assertThat(input.hasComponentType(RelayComponentType.AUTOCRAFTING)).isTrue();
    }

    @Test
    void shouldRemovePatternsWhenNetworkIsRemoved(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);

        // Act
        input.setNetwork(null);

        addPattern(inputAutocrafting, B);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    @SetupNetwork(id = "input_alt")
    void shouldNoLongerReceiveNotificationsFromOldInputNetwork(
        @InjectNetwork("input") final Network inputNetwork,
        @InjectNetwork("input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "input_alt")
        final AutocraftingNetworkComponent inputAlternativeAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        inputNetwork.removeContainer(() -> input);
        inputAlternativeNetwork.addContainer(() -> input);
        input.setNetwork(inputAlternativeNetwork);

        addPattern(inputAlternativeAutocrafting, B);
        addPattern(inputAutocrafting, C);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, C);
        assertThat(inputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(B);
    }

    @Test
    @SetupNetwork(id = "output_alt")
    void shouldNotNotifyOldOutputNetworkWhenOutputNetworkHasChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("output") final Network outputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output_alt")
        final AutocraftingNetworkComponent outputAlternativeAutocrafting,
        @InjectNetwork("output_alt") final Network outputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        outputNetwork.removeContainer(() -> output);
        outputAlternativeNetwork.addContainer(() -> output);
        output.setNetwork(outputAlternativeNetwork);

        addPattern(inputAutocrafting, B);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    @SetupNetwork(id = "output_alt")
    void shouldAddPatternsToNewOutputNetworkIfTheOutputNetworkChanges(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("output") final Network outputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output_alt")
        final AutocraftingNetworkComponent outputAlternativeAutocrafting,
        @InjectNetwork("output_alt") final Network outputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        outputNetwork.removeContainer(() -> output);
        outputAlternativeNetwork.addContainer(() -> output);
        output.setNetwork(outputAlternativeNetwork);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
        assertThat(outputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldRespectAllowlistFilter(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, C));
        input.setFilterMode(FilterMode.ALLOW);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // This update should arrive.
        addPattern(inputAutocrafting, C);
        // This one shouldn't.
        addPattern(inputAutocrafting, D);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B, C, D);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, C);
    }

    @Test
    void shouldRespectFilterNormalizer(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, B));
        input.setFilterMode(FilterMode.ALLOW);
        input.setFilterNormalizer(resource -> {
            if (resource == A_ALTERNATIVE) {
                return A;
            }
            return resource;
        });

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, C);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // These updates should arrive.
        addPattern(inputAutocrafting, A_ALTERNATIVE);
        addPattern(inputAutocrafting, B);
        // This one shouldn't.
        addPattern(inputAutocrafting, D);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B, C, A_ALTERNATIVE, D);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, A_ALTERNATIVE, B);
    }

    @Test
    void shouldUpdateOutputPatternsWhenFiltersAreChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));
        input.setFilters(Set.of(B));

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
    }

    @Test
    void shouldUpdateOutputPatternsWhenFilterModeIsChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));
        input.setFilterMode(FilterMode.ALLOW);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
    }

    @Test
    @SetupNetwork(id = "cycle_input", energyStored = 1, energyCapacity = 2)
    @SetupNetwork(id = "cycle_input_alt", energyStored = 3, energyCapacity = 4)
    void shouldDetectCycles(
        @InjectNetwork("cycle_input") final Network inputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "cycle_input")
        final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("cycle_input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "cycle_input_alt")
        final AutocraftingNetworkComponent inputAlternativeAutocrafting
    ) {
        // Act
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        cycleOutput.setAutocraftingDelegate(inputAlternativeAutocrafting);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        cycleOutputAlternative.setAutocraftingDelegate(inputAutocrafting);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        addPattern(inputAutocrafting, A);
        final Runnable removeB = addPattern(inputAutocrafting, B);
        removeB.run();

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
        assertThat(inputAlternativeAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldUseResourcesFromOutputNetworkForCalculation(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern()
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        final var taskIdFromOutput =
            outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        final var taskIdFromInput =
            inputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);

        // Assert
        assertThat(taskIdFromOutput).isPresent();
        assertThat(taskIdFromInput).isEmpty();
    }

    @Test
    void shouldStartTaskInOutputPatternProvider(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern()
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        final var optionalTaskId =
            outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        assertThat(optionalTaskId).isPresent();

        // Act
        assertThat(output.getTasks()).hasSize(1);
        assertThat(inputAutocrafting.getStatuses()).isEmpty();
        assertThat(outputAutocrafting.getStatuses()).hasSize(1);
    }

    @Test
    void shouldCancelTask(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern()
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act & assert
        final var optionalTaskId =
            outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
        assertThat(optionalTaskId).isPresent();
        final var taskId = optionalTaskId.get();
        assertThat(output.getTasks())
            .hasSize(1)
            .allMatch(task -> task.getId().equals(taskId) && task.getState() == TaskState.READY);

        inputAutocrafting.cancel(taskId);
        assertThat(output.getTasks())
            .hasSize(1)
            .allMatch(task -> task.getId().equals(taskId) && task.getState() == TaskState.READY);

        outputAutocrafting.cancel(taskId);
        assertThat(output.getTasks())
            .hasSize(1)
            .allMatch(task -> task.getId().equals(taskId) && task.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
    }

    @Test
    void shouldStepTasks(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern()
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        assertThat(outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE)).isPresent();

        // Act & assert
        output.doWork();
        assertThat(output.getTasks()).hasSize(1);

        output.doWork();
        assertThat(output.getTasks()).isEmpty();

        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 1)
        );
    }

    @Test
    void shouldStepTasksWithCustomStepBehavior(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern()
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        assertThat(outputAutocrafting.startTask(B, 10, Actor.EMPTY, false, CancellationToken.NONE)).isPresent();

        // Act & assert
        output.doWork();
        assertThat(output.getTasks()).hasSize(1);

        output.doWork();
        assertThat(output.getTasks()).hasSize(1);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 1)
        );

        output.setStepBehavior(new StepBehavior() {
            @Override
            public int getSteps(final Pattern pattern) {
                return 9;
            }
        });

        output.doWork();
        assertThat(output.getTasks()).isEmpty();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 10)
        );
    }

    @Test
    void shouldStepTasksWithExternalPattern(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        inputStorage.addSource(new StorageImpl());

        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern(PatternType.EXTERNAL)
            .ingredient(A, 1)
            .output(B, 1)
            .build());
        final PatternProviderListener listener = mock(PatternProviderListener.class);
        final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
        patternProvider.setSink(sink);
        patternProvider.setListener(listener);
        inputAutocrafting.onContainerAdded(() -> patternProvider);

        outputStorage.addSource(new StorageImpl());
        outputStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        assertThat(outputAutocrafting.startTask(B, 2, Actor.EMPTY, false, CancellationToken.NONE)).isPresent();

        // Act & assert
        output.doWork();
        assertThat(output.getTasks()).hasSize(1);
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(sink.getAll()).isEmpty();

        output.doWork();
        assertThat(output.getTasks()).hasSize(1);
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 1)
        );

        output.doWork();
        assertThat(output.getTasks()).hasSize(1);
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );

        inputStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        output.doWork();
        assertThat(output.getTasks()).hasSize(1);
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );

        outputStorage.insert(B, 2, Action.EXECUTE, Actor.EMPTY);
        output.doWork();
        verify(listener, times(1)).receivedExternalIteration();
        assertThat(output.getTasks()).isEmpty();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(B, 2)
        );
        assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 2)
        );
    }

    @Nested
    @SetupNetwork(id = "output2")
    class NetworkChangeTest {
        @Test
        void shouldInterceptInsertionsOnNewNetworkWhenNetworkChanges(
            @InjectNetworkAutocraftingComponent(networkId = "input")
            final AutocraftingNetworkComponent inputAutocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "output")
            final AutocraftingNetworkComponent outputAutocrafting,
            @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
            @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
            @InjectNetwork("output2") final Network outputNetwork2,
            @InjectNetworkStorageComponent(networkId = "output2") final StorageNetworkComponent outputStorage2
        ) {
            // Arrange
            input.setActive(true);
            input.setOutputNode(output);
            inputStorage.addSource(new StorageImpl());

            final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
            patternProvider.tryUpdatePattern(0, pattern(PatternType.EXTERNAL)
                .ingredient(A, 1)
                .output(B, 1)
                .build());
            final PatternProviderExternalPatternSinkImpl sink = new PatternProviderExternalPatternSinkImpl();
            patternProvider.setSink(sink);
            inputAutocrafting.onContainerAdded(() -> patternProvider);

            outputStorage.addSource(new StorageImpl());
            outputStorage.insert(A, 2, Action.EXECUTE, Actor.EMPTY);

            outputStorage2.addSource(new StorageImpl());

            input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

            assertThat(
                outputAutocrafting.startTask(B, 2, Actor.EMPTY, false, CancellationToken.NONE)).isPresent();

            // Act & assert
            output.doWork();
            assertThat(output.getTasks()).hasSize(1);
            assertThat(outputStorage.getAll()).isEmpty();
            assertThat(sink.getAll()).isEmpty();

            output.doWork();
            assertThat(output.getTasks()).hasSize(1);
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 1)
            );

            output.doWork();
            assertThat(output.getTasks()).hasSize(1);
            assertThat(sink.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new ResourceAmount(A, 2)
            );

            outputStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
            output.doWork();
            assertThat(output.getTasks()).hasSize(1);

            output.setNetwork(outputNetwork2);
            outputNetwork2.addContainer(() -> output);

            outputStorage.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
            output.doWork();
            assertThat(output.getTasks()).hasSize(1);

            outputStorage2.insert(B, 1, Action.EXECUTE, Actor.EMPTY);
            output.doWork();
            assertThat(output.getTasks()).isEmpty();
        }

        @Test
        void shouldNotifyStatusListenersOfOldAndNewNetworkWhenNetworkChanges(
            @InjectNetworkAutocraftingComponent(networkId = "input")
            final AutocraftingNetworkComponent inputAutocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "output")
            final AutocraftingNetworkComponent outputAutocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "output2")
            final AutocraftingNetworkComponent outputAutocrafting2,
            @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
            @InjectNetwork("output") final Network outputNetwork,
            @InjectNetwork("output2") final Network outputNetwork2
        ) {
            // Arrange
            final TaskStatusListener listener = mock(TaskStatusListener.class);
            outputAutocrafting.addListener(listener);

            final TaskStatusListener listener2 = mock(TaskStatusListener.class);
            outputAutocrafting2.addListener(listener2);

            outputStorage.addSource(new StorageImpl());
            outputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
            patternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(B, 1).build());
            inputAutocrafting.onContainerAdded(() -> patternProvider);

            input.setActive(true);
            input.setOutputNode(output);
            input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

            // Act & assert
            final var taskId = outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
            assertThat(taskId).isPresent();
            final ArgumentCaptor<TaskStatus> statusCaptor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(listener, times(1)).taskAdded(statusCaptor.capture());
            verify(listener, never()).taskRemoved(any());
            verify(listener2, never()).taskAdded(any());
            verify(listener2, never()).taskRemoved(any());
            final TaskStatus status = statusCaptor.getValue();
            assertThat(status.info().id()).isEqualTo(taskId.get());

            reset(listener, listener2);

            outputNetwork.removeContainer(() -> output);
            output.setNetwork(outputNetwork2);
            outputNetwork2.addContainer(() -> output);

            verify(listener, never()).taskAdded(any());
            final ArgumentCaptor<TaskId> removedIdCaptor = ArgumentCaptor.forClass(TaskId.class);
            verify(listener, times(1)).taskRemoved(removedIdCaptor.capture());
            final ArgumentCaptor<TaskStatus> addedTaskCaptor = ArgumentCaptor.forClass(TaskStatus.class);
            verify(listener2, times(1)).taskAdded(addedTaskCaptor.capture());
            verify(listener2, never()).taskRemoved(any());
        }

        @Test
        void shouldBeAbleToCancelTaskInNewNetworkWhenNetworkChanges(
            @InjectNetworkAutocraftingComponent(networkId = "input")
            final AutocraftingNetworkComponent inputAutocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "output")
            final AutocraftingNetworkComponent outputAutocrafting,
            @InjectNetworkAutocraftingComponent(networkId = "output2")
            final AutocraftingNetworkComponent outputAutocrafting2,
            @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
            @InjectNetwork("output") final Network outputNetwork,
            @InjectNetwork("output2") final Network outputNetwork2
        ) {
            // Arrange
            final TaskStatusListener listener = mock(TaskStatusListener.class);
            outputAutocrafting.addListener(listener);

            final TaskStatusListener listener2 = mock(TaskStatusListener.class);
            outputAutocrafting2.addListener(listener2);

            outputStorage.addSource(new StorageImpl());
            outputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);

            final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
            patternProvider.tryUpdatePattern(0, pattern().ingredient(A, 1).output(B, 1).build());
            inputAutocrafting.onContainerAdded(() -> patternProvider);

            input.setActive(true);
            input.setOutputNode(output);
            input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

            // Act & assert
            final var taskId = outputAutocrafting.startTask(B, 1, Actor.EMPTY, false, CancellationToken.NONE);
            assertThat(taskId).isPresent();
            assertThat(output.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

            outputNetwork.removeContainer(() -> output);
            output.setNetwork(outputNetwork2);
            outputNetwork2.addContainer(() -> output);

            outputAutocrafting.cancel(taskId.get());
            assertThat(output.getTasks()).hasSize(1).allMatch(t -> t.getState() == TaskState.READY);

            outputAutocrafting2.cancel(taskId.get());
            assertThat(output.getTasks()).hasSize(1)
                .allMatch(t -> t.getState() == TaskState.RETURNING_INTERNAL_STORAGE);
        }
    }
}
