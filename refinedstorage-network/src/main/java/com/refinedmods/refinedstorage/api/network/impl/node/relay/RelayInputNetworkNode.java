package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class RelayInputNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;
    private final Set<RelayComponentType> componentTypes = new HashSet<>();

    @Nullable
    private RelayOutputNetworkNode outputNode;

    public RelayInputNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public void setNetwork(@Nullable final Network network) {
        super.setNetwork(network);
        updateComponents();
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        updateComponents();
    }

    public void setOutputNode(@Nullable final RelayOutputNetworkNode outputNode) {
        this.outputNode = outputNode;
    }

    public void setComponentTypes(final Set<RelayComponentType> componentTypes) {
        this.componentTypes.clear();
        this.componentTypes.addAll(componentTypes);
        updateComponents();
    }

    public void updateComponentType(final RelayComponentType componentType, final boolean enabled) {
        if (enabled) {
            componentTypes.add(componentType);
        } else {
            componentTypes.remove(componentType);
        }
        updateComponents();
    }

    private void updateComponents() {
        if (outputNode == null) {
            return;
        }
        final boolean valid = network != null && isActive();
        final boolean hasEnergy = componentTypes.contains(RelayComponentType.ENERGY);
        outputNode.setEnergyDelegate(valid && hasEnergy
            ? network.getComponent(EnergyNetworkComponent.class)
            : null);
        final boolean hasSecurity = componentTypes.contains(RelayComponentType.SECURITY);
        outputNode.setSecurityDelegate(valid && hasSecurity
            ? network.getComponent(SecurityNetworkComponent.class)
            : null);
        final boolean hasStorage = componentTypes.contains(RelayComponentType.STORAGE);
        outputNode.setStorageDelegate(valid && hasStorage
            ? network.getComponent(StorageNetworkComponent.class)
            : null);
    }

    public void setAccessMode(final AccessMode accessMode) {
        if (outputNode != null) {
            outputNode.setAccessMode(accessMode);
        }
    }

    public void setPriority(final int priority) {
        if (outputNode != null) {
            outputNode.setPriority(priority);
        }
    }

    public void setFilters(final Set<ResourceKey> filters) {
        if (outputNode != null) {
            outputNode.setFilters(filters);
        }
    }

    public void setFilterMode(final FilterMode filterMode) {
        if (outputNode != null) {
            outputNode.setFilterMode(filterMode);
        }
    }

    public void setFilterNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        if (outputNode != null) {
            outputNode.setFilterNormalizer(normalizer);
        }
    }

    public boolean hasComponentType(final RelayComponentType componentType) {
        return componentTypes.contains(componentType);
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }
}
