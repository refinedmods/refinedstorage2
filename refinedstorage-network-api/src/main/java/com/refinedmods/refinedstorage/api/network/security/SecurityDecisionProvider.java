package com.refinedmods.refinedstorage.api.network.security;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
@FunctionalInterface
public interface SecurityDecisionProvider {
    SecurityDecision isAllowed(Permission permission, SecurityActor actor);

    default SecurityDecision isAllowed(Permission permission) {
        return SecurityDecision.PASS;
    }

    default boolean isProviderActive() {
        return true;
    }

    default boolean contains(SecurityNetworkComponent component) {
        return false;
    }
}
