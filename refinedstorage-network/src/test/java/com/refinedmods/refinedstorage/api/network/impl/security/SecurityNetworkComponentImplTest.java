package com.refinedmods.refinedstorage.api.network.impl.security;

import com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.network.test.fixtures.PermissionFixtures;
import com.refinedmods.refinedstorage.network.test.fixtures.SecurityActorFixtures;
import com.refinedmods.refinedstorage.network.test.nodefactory.SimpleNetworkNodeFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode.activeSecurityDecisionProvider;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityNetworkComponentImplTest {
    SecurityNetworkComponent sut;
    SecurityDecisionProviderImpl securityDecisionProvider;
    SecurityDecisionProviderProxyNetworkNode node;

    @BeforeEach
    void setUp() {
        sut = new SecurityNetworkComponentImpl(SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT));
        securityDecisionProvider = new SecurityDecisionProviderImpl();
        node = activeSecurityDecisionProvider(securityDecisionProvider);
    }

    @Test
    void shouldUseDefaultPolicyIfNoSecurityDecisionProvidersArePresent() {
        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();
    }

    @Test
    void shouldDenyAllIfAtLeastOneSecurityDecisionProviderIsPresent() {
        // Arrange
        sut.onContainerAdded(() -> node);

        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();
    }

    @Test
    void shouldUseDefaultPolicyIfAllSecurityDecisionProvidersAreInactive() {
        // Arrange
        sut.onContainerAdded(() -> new SecurityDecisionProviderProxyNetworkNode(
            SimpleNetworkNodeFactory.TYPE,
            0,
            new SecurityDecisionProviderImpl().setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();
    }

    @Test
    void shouldAllowOrDeny() {
        // Arrange
        securityDecisionProvider.setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER));
        sut.onContainerAdded(() -> node);

        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();
    }

    @Test
    void shouldOnlyAllowIfAllSecurityDecisionProvidersAllow() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER2))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.B, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        sut.onContainerAdded(() -> new SecurityDecisionProviderProxyNetworkNode(
            SimpleNetworkNodeFactory.TYPE,
            0,
            new SecurityDecisionProviderImpl()
                .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT))
                .setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.OTHER, PermissionFixtures.OTHER2))
        ));

        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.C)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.C)).isFalse();
    }

    @Test
    void shouldUseDefaultPolicyOfSecurityDecisionProviderIfAllProvidersPassDecision() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
            .setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER))
            .setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT, PermissionFixtures.OTHER2))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(SecurityActorFixtures.C, SecurityPolicy.of(PermissionFixtures.OTHER))
        ));

        // Act & assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.B)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.B)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.B)).isFalse();

        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.C)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.C)).isTrue();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.C)).isFalse();
    }

    @Test
    void shouldRemoveContainer() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.ALLOW_BY_DEFAULT))
        ));

        final var removedNode = activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.OTHER)));
        sut.onContainerAdded(() -> removedNode);

        // Act
        sut.onContainerRemoved(() -> removedNode);

        // Assert
        assertThat(sut.isAllowed(PermissionFixtures.ALLOW_BY_DEFAULT, SecurityActorFixtures.A)).isTrue();
    }

    @Test
    void shouldClearPolicies() {
        // Arrange
        sut.onContainerAdded(() -> node);
        securityDecisionProvider.setPolicy(SecurityActorFixtures.A, SecurityPolicy.of(PermissionFixtures.OTHER));
        securityDecisionProvider.setDefaultPolicy(SecurityPolicy.of(PermissionFixtures.OTHER2));

        // Act
        securityDecisionProvider.clearPolicies();

        // Assert
        assertThat(sut.isAllowed(PermissionFixtures.OTHER, SecurityActorFixtures.A)).isFalse();
        assertThat(sut.isAllowed(PermissionFixtures.OTHER2, SecurityActorFixtures.A)).isTrue();
    }
}
