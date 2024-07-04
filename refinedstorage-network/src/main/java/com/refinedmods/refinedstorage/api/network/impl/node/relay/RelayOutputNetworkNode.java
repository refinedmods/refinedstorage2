package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecision;
import com.refinedmods.refinedstorage.api.network.security.SecurityDecisionProvider;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class RelayOutputNetworkNode extends AbstractNetworkNode
    implements EnergyProvider, SecurityDecisionProvider, StorageProvider {
    private final long energyUsage;
    private final RelayOutputStorage storage = new RelayOutputStorage();

    @Nullable
    private EnergyNetworkComponent energyDelegate;
    @Nullable
    private SecurityNetworkComponent securityDelegate;

    public RelayOutputNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    void setEnergyDelegate(@Nullable final EnergyNetworkComponent energyDelegate) {
        this.energyDelegate = energyDelegate;
    }

    void setSecurityDelegate(@Nullable final SecurityNetworkComponent securityDelegate) {
        this.securityDelegate = securityDelegate;
    }

    void setStorageDelegate(@Nullable final StorageNetworkComponent storageDelegate) {
        this.storage.setDelegate(storageDelegate);
    }

    void setAccessMode(final AccessMode accessMode) {
        this.storage.setAccessMode(accessMode);
    }

    void setPriority(final int priority) {
        this.storage.setPriority(priority);
        if (network != null) {
            network.getComponent(StorageNetworkComponent.class).sortSources();
        }
    }

    void setFilters(final Set<ResourceKey> filters) {
        this.storage.setFilters(filters);
    }

    void setFilterMode(final FilterMode filterMode) {
        this.storage.setFilterMode(filterMode);
    }

    void setFilterNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        this.storage.setFilterNormalizer(normalizer);
    }

    @Override
    public long getEnergyUsage() {
        if (energyDelegate != null || securityDelegate != null || storage.hasDelegate()) {
            return energyUsage;
        }
        return 0;
    }

    @Override
    public long getStored() {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.getStored();
    }

    @Override
    public long getCapacity() {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.getCapacity();
    }

    @Override
    public long extract(final long amount) {
        return energyDelegate == null || energyDelegate.contains(energyDelegate) ? 0 : energyDelegate.extract(amount);
    }

    @Override
    public boolean contains(final EnergyProvider energyProvider) {
        return energyProvider == energyDelegate
            || (energyDelegate != null && energyDelegate.contains(energyProvider));
    }

    @Override
    public boolean contains(final SecurityNetworkComponent securityComponent) {
        return securityComponent == securityDelegate
            || (securityDelegate != null && securityDelegate.contains(securityComponent));
    }

    @Override
    public SecurityDecision isAllowed(final Permission permission, final SecurityActor actor) {
        if (securityDelegate == null || securityDelegate.contains(securityDelegate)) {
            return SecurityDecision.PASS;
        }
        return securityDelegate.isAllowed(permission, actor) ? SecurityDecision.ALLOW : SecurityDecision.DENY;
    }

    @Override
    public boolean isProviderActive() {
        return isActive() && securityDelegate != null;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }
}
