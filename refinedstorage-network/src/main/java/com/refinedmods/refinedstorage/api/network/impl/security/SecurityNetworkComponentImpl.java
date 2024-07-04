package com.refinedmods.refinedstorage.api.network.impl.security;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecision;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecisionProvider;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityNetworkComponentImpl implements SecurityNetworkComponent {
    private final Set<SecurityDecisionProvider> providers = new LinkedHashSet<>();
    private final SecurityPolicy defaultPolicy;

    public SecurityNetworkComponentImpl(final SecurityPolicy defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof SecurityDecisionProvider provider) {
            providers.add(provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof SecurityDecisionProvider provider) {
            providers.remove(provider);
        }
    }

    @Override
    public boolean isAllowed(final Permission permission, final SecurityActor actor) {
        final Set<SecurityDecisionProvider> activeProviders = providers.stream()
            .filter(SecurityDecisionProvider::isProviderActive)
            .collect(Collectors.toSet());
        if (activeProviders.isEmpty()) {
            return defaultPolicy.isAllowed(permission);
        }
        final Set<SecurityDecision> decisions = activeProviders.stream().map(provider ->
            CoreValidations.validateNotNull(provider.isAllowed(permission, actor), "Decision cannot be null")
        ).collect(Collectors.toSet());
        final boolean anyDenied = decisions.stream().anyMatch(decision -> decision == SecurityDecision.DENY);
        if (anyDenied) {
            return false;
        }
        final boolean anyAllowed = decisions.stream().anyMatch(decision -> decision == SecurityDecision.ALLOW);
        if (anyAllowed) {
            return true;
        }
        return tryFallback(permission, activeProviders);
    }

    @Override
    public boolean contains(final SecurityNetworkComponent component) {
        for (final SecurityDecisionProvider provider : providers) {
            if (provider.contains(component)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryFallback(final Permission permission, final Set<SecurityDecisionProvider> activeProviders) {
        final Set<SecurityDecision> decisions = activeProviders.stream().map(provider ->
            CoreValidations.validateNotNull(provider.isAllowed(permission), "Decision cannot be null")
        ).collect(Collectors.toSet());
        final boolean anyDenied = decisions.stream().anyMatch(decision -> decision == SecurityDecision.DENY);
        if (anyDenied) {
            return false;
        }
        return decisions.stream().anyMatch(decision -> decision == SecurityDecision.ALLOW);
    }
}
