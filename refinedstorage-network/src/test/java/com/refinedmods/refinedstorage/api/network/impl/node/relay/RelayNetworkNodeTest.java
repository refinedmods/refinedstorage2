package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkSecurityComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fake.FakePermissions;
import com.refinedmods.refinedstorage.network.test.fake.FakeSecurityActors;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode.activeSecurityDecisionProvider;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.B;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.C;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayNetworkNodeTest {
    private static final long INPUT_ENERGY_USAGE = 5;
    private static final long OUTPUT_ENERGY_USAGE = 5;

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
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        input.setAccessMode(AccessMode.INSERT_EXTRACT);
        input.setPriority(5);
        input.setFilters(Set.of(A, B, C));

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldNotPassComponentsIfInactive(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setOutputNode(output);

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldNotPassComponentsIfNoNetworkIsSet(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setNetwork(null);
        input.setOutputNode(output);

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldResetComponentsIfBecomingInactive(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        input.setActive(false);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    void shouldResetComponentsIfNetworkIsRemoved(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        input.setNetwork(null);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(output.getEnergyUsage()).isZero();
    }

    @Test
    @SetupNetwork(id = "input_alt", energyStored = 123, energyCapacity = 456)
    void shouldResetComponentsIfNetworkIsChanged(
        @InjectNetwork("input") final Network inputNetwork,
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetwork("input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkEnergyComponent(networkId = "input_alt") final EnergyNetworkComponent inputAlternativeEnergy,
        @InjectNetworkSecurityComponent(networkId = "input_alt")
        final SecurityNetworkComponent inputAlternativeSecurity,
        @InjectNetworkStorageComponent(networkId = "input_alt") final StorageNetworkComponent inputAlternativeStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.ENERGY,
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        inputStorage.addSource(new InMemoryStorageImpl());
        inputStorage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
        inputStorage.insert(B, 5, Action.EXECUTE, EmptyActor.INSTANCE);
        inputStorage.extract(B, 3, Action.EXECUTE, EmptyActor.INSTANCE);

        inputAlternativeStorage.addSource(new InMemoryStorageImpl());
        inputAlternativeStorage.insert(A, 33, Action.EXECUTE, EmptyActor.INSTANCE);

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addSecurityPolicy(inputAlternativeSecurity, FakePermissions.OTHER2);

        // Act
        inputNetwork.removeContainer(() -> input);
        input.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> input);

        final long originalStored = inputAlternativeEnergy.getStored();
        final long extractedEnergy = output.extract(1);

        final long insertedStorage = outputStorage.insert(C, 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long extractedStorage = outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extractedEnergy).isEqualTo(1);
        assertThat(insertedStorage).isEqualTo(1);
        assertThat(extractedStorage).isEqualTo(1);

        assertThat(outputEnergy.getCapacity()).isEqualTo(inputAlternativeEnergy.getCapacity());
        assertThat(outputEnergy.getStored()).isEqualTo(originalStored - 1);
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isTrue();
        assertThat(outputStorage.getStored()).isEqualTo(33);
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount(A, 32),
            new ResourceAmount(C, 1)
        );
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
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        final long originalStored = inputEnergy.getStored();
        input.updateComponentType(RelayComponentType.ENERGY, true);
        final long extracted = output.extract(10);

        // Assert
        assertThat(outputEnergy.getCapacity()).isEqualTo(inputEnergy.getCapacity());
        assertThat(outputEnergy.getStored()).isEqualTo(originalStored - 10);
        assertThat(extracted).isEqualTo(10);
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isTrue();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isEqualTo(1);
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isEqualTo(1);
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    @Test
    void shouldResetComponentsWhenComponentTypeIsDisabled(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setComponentTypes(Set.of(
            RelayComponentType.SECURITY,
            RelayComponentType.STORAGE
        ));

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        // Act
        input.updateComponentType(RelayComponentType.ENERGY, false);
        final long extracted = output.extract(10);

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(extracted).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isTrue();
        assertThat(outputStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 10)
        );
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isEqualTo(1);
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isEqualTo(1);
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    @ParameterizedTest
    @EnumSource(RelayComponentType.class)
    void shouldUseEnergyWhenAtLeastOneComponentIsActive(final RelayComponentType type) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        // Act
        input.setComponentTypes(Set.of(type));

        // Assert
        assertThat(output.getEnergyUsage()).isEqualTo(OUTPUT_ENERGY_USAGE);
    }

    static void addSecurityPolicy(final SecurityNetworkComponent security, final FakePermissions permission) {
        security.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(permission))
        ));
    }

    static void addStorageSource(final StorageNetworkComponent storage) {
        storage.addSource(new InMemoryStorageImpl());
        storage.insert(A, 10, Action.EXECUTE, EmptyActor.INSTANCE);
    }
}
