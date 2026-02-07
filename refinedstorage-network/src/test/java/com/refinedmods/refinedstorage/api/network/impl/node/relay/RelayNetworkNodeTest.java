package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkSecurityComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.PermissionFixtures;
import com.refinedmods.refinedstorage.network.test.fixtures.SecurityActorFixtures;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode.activeSecurityDecisionProvider;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayNetworkNodeTest {
    private static final long INPUT_ENERGY_USAGE = 5;
    private static final long OUTPUT_ENERGY_USAGE = 5;

    @SuppressWarnings("DefaultAnnotationParam")
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = INPUT_ENERGY_USAGE),
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = OUTPUT_ENERGY_USAGE)
    }, networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void testInitialState() {
        assertThat(input.getEnergyUsage()).isEqualTo(INPUT_ENERGY_USAGE);
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldNotPassComponentsIfOutputNodeIsNotSet(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        input.setAccessMode(AccessMode.INSERT_EXTRACT);
        input.setInsertPriority(5);
        input.setFilters(Set.of(A, B, C));

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
        assertThat(outputAutocrafting.getPatterns()).isEmpty();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldNotPassComponentsIfInactive(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setOutputNode(output);

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
        assertThat(outputAutocrafting.getPatterns()).isEmpty();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldNotPassComponentsIfNoNetworkIsSet(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setNetwork(null);
        input.setOutputNode(output);

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
        assertThat(outputAutocrafting.getPatterns()).isEmpty();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldResetComponentsIfBecomingInactive(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        input.setActive(false);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
        assertThat(outputAutocrafting.getPatterns()).isEmpty();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldResetComponentsIfNetworkIsRemoved(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        input.setNetwork(null);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
        assertThat(outputAutocrafting.getPatterns()).isEmpty();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    @SetupNetwork(id = "input_alt", energyStored = 123, energyCapacity = 456)
    void shouldResetComponentsIfNetworkIsChanged(
        @InjectNetwork("input") final Network inputNetwork,
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkEnergyComponent(networkId = "input_alt") final EnergyNetworkComponent inputAlternativeEnergy,
        @InjectNetworkSecurityComponent(networkId = "input_alt")
        final SecurityNetworkComponent inputAlternativeSecurity,
        @InjectNetworkStorageComponent(networkId = "input_alt") final StorageNetworkComponent inputAlternativeStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input_alt")
        final AutocraftingNetworkComponent inputAlternativeAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        inputStorage.addSource(new StorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
        inputStorage.insert(B, 5, Action.EXECUTE, Actor.EMPTY);
        inputStorage.extract(B, 3, Action.EXECUTE, Actor.EMPTY);

        inputAlternativeStorage.addSource(new StorageImpl());
        inputAlternativeStorage.insert(A, 33, Action.EXECUTE, Actor.EMPTY);

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addSecurityPolicy(inputAlternativeSecurity, PermissionFixtures.OTHER2);

        addPattern(inputAutocrafting, A);
        addPattern(inputAlternativeAutocrafting, B);

        // Act
        inputNetwork.removeContainer(() -> input);
        input.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> input);

        final long originalStored = inputAlternativeEnergy.getStored();
        final long extractedEnergy = output.extract(1);

        final long insertedStorage = outputStorage.insert(C, 1, Action.EXECUTE, Actor.EMPTY);
        final long extractedStorage = outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY);

        // Assert
        assertThat(extractedEnergy).isEqualTo(1);
        assertThat(insertedStorage).isEqualTo(1);
        assertThat(extractedStorage).isEqualTo(1);

        assertThat(outputEnergy.getCapacity()).isEqualTo(inputAlternativeEnergy.getCapacity());
        assertThat(outputEnergy.getStored()).isEqualTo(originalStored - 1);
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isTrue();
        assertThat(outputStorage.getStored()).isEqualTo(33);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 32),
            new ResourceAmount(C, 1)
        );
        assertThat(outputAutocrafting.getOutputs()).containsExactly(B);
        assertThat(outputAutocrafting.getPatterns()).allMatch(p -> p.layout().outputs()
            .stream()
            .anyMatch(patternOutput -> patternOutput.resource().equals(B)));
        assertThat(inputAlternativeStorage.getAll()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 32), new ResourceAmount(C, 1));
        assertThat(inputStorage.getAll()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(new ResourceAmount(A, 10), new ResourceAmount(B, 2));
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    @Test
    void shouldResetComponentsWhenComponentTypeIsEnabled(
        @InjectNetworkEnergyComponent(networkId = "input") final EnergyNetworkComponent inputEnergy,
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        final long originalStored = inputEnergy.getStored();
        input.updateComponentType(RelayComponentType.ENERGY, true);
        final long extracted = output.extract(10);

        // Assert
        assertThat(outputEnergy.getCapacity()).isEqualTo(inputEnergy.getCapacity());
        assertThat(outputEnergy.getStored()).isEqualTo(originalStored - 10);
        assertThat(extracted).isEqualTo(10);
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isTrue();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isEqualTo(1);
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isEqualTo(1);
        assertThat(outputAutocrafting.getOutputs()).containsExactly(A);
        assertThat(outputAutocrafting.getPatterns()).allMatch(p -> p.layout().outputs()
            .stream()
            .anyMatch(patternOutput -> patternOutput.resource().equals(A)));
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    @Test
    void shouldResetComponentsWhenComponentTypeIsDisabled(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE,
            RelayComponentType.AUTOCRAFTING
        ));

        addSecurityPolicy(inputSecurity, PermissionFixtures.OTHER);
        addStorageSource(inputStorage);
        addPattern(inputAutocrafting, A);

        // Act
        input.updateComponentType(RelayComponentType.ENERGY, false);
        final long extracted = output.extract(10);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(extracted).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isTrue();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isEqualTo(1);
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isEqualTo(1);
        assertThat(outputAutocrafting.getOutputs()).containsExactly(A);
        assertThat(outputAutocrafting.getPatterns()).allMatch(p -> p.layout().outputs()
            .stream()
            .anyMatch(patternOutput -> patternOutput.resource().equals(A)));
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    private static Stream<RelayComponentType<?>> provideComponentTypes() {
        return RelayComponentType.ALL.stream();
    }

    @ParameterizedTest
    @MethodSource("provideComponentTypes")
    void shouldUseEnergyWhenAtLeastOneComponentIsActive(final RelayComponentType<?> type) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        // Act
        input.setComponentTypes(Set.of(type));

        // Assert
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    static void addSecurityPolicy(final SecurityNetworkComponent security, final PermissionFixtures permission) {
        security.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(permission))
        ));
    }

    static void addStorageSource(final StorageNetworkComponent storage) {
        storage.addSource(new StorageImpl());
        storage.insert(A, 10, Action.EXECUTE, Actor.EMPTY);
    }

    static Runnable addPattern(final AutocraftingNetworkComponent component, final ResourceKey output) {
        final Pattern pattern = pattern().ingredient(C, 1).output(output, 1).build();
        final PatternProviderNetworkNode patternProvider = new PatternProviderNetworkNode(0, 1);
        patternProvider.tryUpdatePattern(0, pattern);
        final NetworkNodeContainer container = () -> patternProvider;
        component.onContainerAdded(container);
        return () -> component.onContainerRemoved(container);
    }
}
