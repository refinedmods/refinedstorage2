package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMap;
import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

public class NetworkImpl implements Network {
    private final ComponentMap<NetworkComponent> componentMap;

    public NetworkImpl(ComponentMapFactory<NetworkComponent, Network> componentMapFactory) {
        this.componentMap = componentMapFactory.buildComponentMap(this);
    }

    @Override
    public void addContainer(NetworkNodeContainer container) {
        componentMap.getComponents().forEach(c -> c.onContainerAdded(container));
    }

    @Override
    public void removeContainer(NetworkNodeContainer container) {
        componentMap.getComponents().forEach(c -> c.onContainerRemoved(container));
    }

    @Override
    public void remove() {
        componentMap.getComponents().forEach(NetworkComponent::onNetworkRemoved);
    }

    @Override
    public void split(Set<Network> networks) {
        componentMap.getComponents().forEach(c -> c.onNetworkSplit(networks));
    }

    @Override
    public void merge(Network network) {
        componentMap.getComponents().forEach(c -> c.onNetworkMergedWith(network));
    }

    @Override
    public <I extends NetworkComponent> I getComponent(Class<I> componentType) {
        return componentMap.getComponent(componentType);
    }
}
