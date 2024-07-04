package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
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

import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addSecurityPolicy;
import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addStorageSource;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayEnergyNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void shouldPassEnergyComponent(
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

        addSecurityPolicy(inputSecurity, FakePermissions.OTHER);
        addStorageSource(inputStorage);

        final long originalStored = inputEnergy.getStored();

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.ENERGY));
        final long extracted = output.extract(10);

        // Assert
        assertThat(extracted).isEqualTo(10);
        assertThat(outputEnergy.getCapacity()).isEqualTo(inputEnergy.getCapacity());
        assertThat(outputEnergy.getStored()).isEqualTo(originalStored - 10);
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(input.hasComponentType(RelayComponentType.ENERGY)).isTrue();
        assertThat(input.hasComponentType(RelayComponentType.SECURITY)).isFalse();
        assertThat(input.hasComponentType(RelayComponentType.STORAGE)).isFalse();
    }

    @Test
    @SetupNetwork(id = "cycle_input", energyStored = 1, energyCapacity = 2)
    @SetupNetwork(id = "cycle_input_alt", energyStored = 3, energyCapacity = 4)
    void shouldDetectEnergyCycles(
        @InjectNetwork("cycle_input") final Network inputNetwork,
        @InjectNetworkEnergyComponent(networkId = "cycle_input") final EnergyNetworkComponent inputEnergy,
        @InjectNetwork("cycle_input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkEnergyComponent(networkId = "cycle_input_alt") final EnergyNetworkComponent inputAlternativeEnergy
    ) {
        // Arrange
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        cycleOutput.setEnergyDelegate(inputAlternativeEnergy);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        cycleOutputAlternative.setEnergyDelegate(inputEnergy);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        // Assert
        assertThat(inputEnergy.getStored()).isEqualTo(1);
        assertThat(inputAlternativeEnergy.getStored()).isEqualTo(3);

        assertThat(inputEnergy.getCapacity()).isEqualTo(2);
        assertThat(inputAlternativeEnergy.getCapacity()).isEqualTo(4);

        assertThat(inputEnergy.extract(10)).isEqualTo(1);
        assertThat(inputAlternativeEnergy.extract(10)).isEqualTo(3);
    }
}
