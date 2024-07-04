package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
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

import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addStorageSource;
import static com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode.activeSecurityDecisionProvider;
import static com.refinedmods.refinedstorage.network.test.fake.FakeResources.A;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelaySecurityNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void shouldPassSecurityComponent(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkStorageComponent(networkId = "input") final StorageNetworkComponent inputStorage,
        @InjectNetworkEnergyComponent(networkId = "output") final EnergyNetworkComponent outputEnergy,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity,
        @InjectNetworkStorageComponent(networkId = "output") final StorageNetworkComponent outputStorage
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        inputSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(
            new SecurityDecisionProviderImpl()
                .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT))
                .setPolicy(FakeSecurityActors.B, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        addStorageSource(inputStorage);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.SECURITY));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isTrue();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, EmptyActor.INSTANCE)).isZero();
        assertThat(input.hasComponentType(RelayComponentType.ENERGY)).isFalse();
        assertThat(input.hasComponentType(RelayComponentType.SECURITY)).isTrue();
        assertThat(input.hasComponentType(RelayComponentType.STORAGE)).isFalse();
    }

    @Test
    void shouldNotActAsSecurityDecisionProviderIfOutputIsDisabled(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        inputSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        input.setComponentTypes(Set.of(RelayComponentType.SECURITY));

        // Act
        output.setActive(false);

        // Assert
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
    }

    @Test
    void shouldNotActAsSecurityDecisionProviderIfSecurityIsNotPassed(
        @InjectNetworkSecurityComponent(networkId = "input") final SecurityNetworkComponent inputSecurity,
        @InjectNetworkSecurityComponent(networkId = "output") final SecurityNetworkComponent outputSecurity
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        inputSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        // Act
        input.setComponentTypes(Set.of());

        // Assert
        assertThat(outputSecurity.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(outputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
    }

    @Test
    @SetupNetwork(id = "cycle_input")
    @SetupNetwork(id = "cycle_input_alt")
    void shouldDetectSecurityCycles(
        @InjectNetwork("cycle_input") final Network inputNetwork,
        @InjectNetworkSecurityComponent(networkId = "cycle_input") final SecurityNetworkComponent inputSecurity,
        @InjectNetwork("cycle_input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkSecurityComponent(networkId = "cycle_input_alt")
        final SecurityNetworkComponent inputAlternativeSecurity
    ) {
        // Arrange
        inputSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(
            new SecurityDecisionProviderImpl()
                .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        inputAlternativeSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(
            new SecurityDecisionProviderImpl()
                .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER2))
        ));

        // Act
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        cycleOutput.setSecurityDelegate(inputAlternativeSecurity);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        cycleOutputAlternative.setSecurityDelegate(inputSecurity);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        // Assert
        assertThat(inputSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isTrue();
        assertThat(inputSecurity.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(inputAlternativeSecurity.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(inputAlternativeSecurity.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isTrue();
    }
}
