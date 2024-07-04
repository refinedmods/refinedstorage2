package com.refinedmods.refinedstorage.api.network.impl.security;

import com.refinedmods.refinedstorage.api.network.impl.node.security.SecurityDecisionProviderProxyNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.network.test.fake.FakePermissions;
import com.refinedmods.refinedstorage.network.test.fake.FakeSecurityActors;

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
        sut = new SecurityNetworkComponentImpl(SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT));
        securityDecisionProvider = new SecurityDecisionProviderImpl();
        node = activeSecurityDecisionProvider(securityDecisionProvider);
    }

    @Test
    void shouldUseDefaultPolicyIfNoSecurityDecisionProvidersArePresent() {
        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();
    }

    @Test
    void shouldDenyAllIfAtLeastOneSecurityDecisionProviderIsPresent() {
        // Arrange
        sut.onContainerAdded(() -> node);

        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();
    }

    @Test
    void shouldUseDefaultPolicyIfAllSecurityDecisionProvidersAreInactive() {
        // Arrange
        sut.onContainerAdded(() -> new SecurityDecisionProviderProxyNetworkNode(0, new SecurityDecisionProviderImpl()
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.OTHER))));

        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();
    }

    @Test
    void shouldAllowOrDeny() {
        // Arrange
        securityDecisionProvider.setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER));
        sut.onContainerAdded(() -> node);

        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();
    }

    @Test
    void shouldOnlyAllowIfAllSecurityDecisionProvidersAllow() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER2))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.B, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        sut.onContainerAdded(() -> new SecurityDecisionProviderProxyNetworkNode(0, new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT))
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.OTHER, FakePermissions.OTHER2))));

        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.C)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.C)).isFalse();
    }

    @Test
    void shouldUseDefaultPolicyOfSecurityDecisionProviderIfAllProvidersPassDecision() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER))
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT, FakePermissions.OTHER2))
        ));

        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setPolicy(FakeSecurityActors.C, SecurityPolicy.of(FakePermissions.OTHER))
        ));

        // Act & assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.B)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.B)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.B)).isFalse();

        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.C)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.C)).isTrue();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.C)).isFalse();
    }

    @Test
    void shouldRemoveContainer() {
        // Arrange
        sut.onContainerAdded(() -> activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.ALLOW_BY_DEFAULT))
        ));

        final var removedNode = activeSecurityDecisionProvider(new SecurityDecisionProviderImpl()
            .setDefaultPolicy(SecurityPolicy.of(FakePermissions.OTHER)));
        sut.onContainerAdded(() -> removedNode);

        // Act
        sut.onContainerRemoved(() -> removedNode);

        // Assert
        assertThat(sut.isAllowed(FakePermissions.ALLOW_BY_DEFAULT, FakeSecurityActors.A)).isTrue();
    }

    @Test
    void shouldClearPolicies() {
        // Arrange
        sut.onContainerAdded(() -> node);
        securityDecisionProvider.setPolicy(FakeSecurityActors.A, SecurityPolicy.of(FakePermissions.OTHER));
        securityDecisionProvider.setDefaultPolicy(SecurityPolicy.of(FakePermissions.OTHER2));

        // Act
        securityDecisionProvider.clearPolicies();

        // Assert
        assertThat(sut.isAllowed(FakePermissions.OTHER, FakeSecurityActors.A)).isFalse();
        assertThat(sut.isAllowed(FakePermissions.OTHER2, FakeSecurityActors.A)).isTrue();
    }
}
