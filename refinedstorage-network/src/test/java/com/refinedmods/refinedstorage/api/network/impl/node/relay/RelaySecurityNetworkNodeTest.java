package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityDecisionProviderImpl;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkEnergyComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkSecurityComponent;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.fixtures.PermissionFixtures;
import com.refinedmods.refinedstorage.network.test.fixtures.SecurityActorFixtures;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addStorageSource;
import static com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode.activeSecurityDecisionProvider;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelaySecurityNetworkNodeTest {
    @SuppressWarnings("DefaultAnnotationParam")
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
                .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT))
                .setPolicy(SecurityActorFixtures.B, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        addStorageSource(inputStorage);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.SECURITY));

        // Assert
        assertThat(outputEnergy.getCapacity()).isZero();
        assertThat(outputEnergy.getStored()).isZero();
        assertThat(outputEnergy.extract(1)).isZero();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isTrue();
        assertThat(outputStorage.getAll()).isEmpty();
        assertThat(outputStorage.insert(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
        assertThat(outputStorage.extract(A, 1, Action.EXECUTE, Actor.EMPTY)).isZero();
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
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        input.setComponentTypes(Set.of(RelayComponentType.SECURITY));

        // Act
        output.setActive(false);

        // Assert
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
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
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        // Act
        input.setComponentTypes(Set.of());

        // Assert
        assertThat(outputSecurity.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(outputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
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
                .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        inputAlternativeSecurity.onContainerAdded(() -> activeSecurityDecisionProvider(
            new SecurityDecisionProviderImpl()
                .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER2))
        ));

        // Act
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        final ExternalPatternSinkKey cycleOutputKey = new ExternalPatternSinkKey() {
        };
        cycleOutput.setSinkKeyProvider(() -> cycleOutputKey);
        cycleOutput.setSecurityDelegate(inputAlternativeSecurity);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        final ExternalPatternSinkKey cycleOutputAlternativeKey = new ExternalPatternSinkKey() {
        };
        cycleOutputAlternative.setSinkKeyProvider(() -> cycleOutputAlternativeKey);
        cycleOutputAlternative.setSecurityDelegate(inputSecurity);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        // Assert
        assertThat(inputSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isTrue();
        assertThat(inputSecurity.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(inputAlternativeSecurity.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(inputAlternativeSecurity.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isTrue();
    }
}
