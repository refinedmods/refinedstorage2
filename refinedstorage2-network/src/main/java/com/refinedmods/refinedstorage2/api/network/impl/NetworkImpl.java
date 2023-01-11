package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMap;
import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

public class NetworkImpl implements Network {
    private final ComponentMap<NetworkComponent> componentMap;

    public NetworkImpl(final ComponentMapFactory<NetworkComponent, Network> componentMapFactory) {
        this.componentMap = componentMapFactory.buildComponentMap(this);
    }

    @Override
    public void addContainer(final NetworkNodeContainer container) {
        componentMap.getComponents().forEach(c -> c.onContainerAdded(container));
    }

    @Override
    public void removeContainer(final NetworkNodeContainer container) {
        componentMap.getComponents().forEach(c -> c.onContainerRemoved(container));
    }

    @Override
    public void remove() {
        componentMap.getComponents().forEach(NetworkComponent::onNetworkRemoved);
    }

    @Override
    public void split(final Set<Network> networks) {
        componentMap.getComponents().forEach(c -> c.onNetworkSplit(networks));
    }

    @Override
    public void merge(final Network network) {
        componentMap.getComponents().forEach(c -> c.onNetworkMergedWith(network));
    }

    @Override
    public <I extends NetworkComponent> I getComponent(final Class<I> componentType) {
        return componentMap.getComponent(componentType);
    }
}
