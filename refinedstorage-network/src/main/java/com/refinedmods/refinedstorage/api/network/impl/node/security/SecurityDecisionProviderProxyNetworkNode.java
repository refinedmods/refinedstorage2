package com.refinedmods.refinedstorage.api.network.impl.node.security;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecision;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecisionProvider;

import javax.annotation.Nullable;

public class SecurityDecisionProviderProxyNetworkNode extends AbstractNetworkNode implements SecurityDecisionProvider {
    private long energyUsage;
    @Nullable
    private SecurityDecisionProvider delegate;

    public SecurityDecisionProviderProxyNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public SecurityDecisionProviderProxyNetworkNode(final long energyUsage, final SecurityDecisionProvider delegate) {
        this(energyUsage);
        this.delegate = delegate;
    }

    public void setDelegate(@Nullable final SecurityDecisionProvider delegate) {
        this.delegate = delegate;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission, final SecurityActor actor) {
        if (delegate == null) {
            return SecurityDecision.PASS;
        }
        return delegate.isAllowed(permission, actor);
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission) {
        if (delegate == null) {
            return SecurityDecision.PASS;
        }
        return delegate.isAllowed(permission);
    }

    @Override
    public boolean isProviderActive() {
        return isActive();
    }

    public static SecurityDecisionProviderProxyNetworkNode activeSecurityDecisionProvider(
        final SecurityDecisionProvider provider
    ) {
        final SecurityDecisionProviderProxyNetworkNode node = new SecurityDecisionProviderProxyNetworkNode(0, provider);
        node.setActive(true);
        return node;
    }
}
