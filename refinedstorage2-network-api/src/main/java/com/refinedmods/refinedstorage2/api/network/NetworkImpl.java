package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Map;
import java.util.Set;

public class NetworkImpl implements Network {
    private final Map<Class<? extends NetworkComponent>, NetworkComponent> components;

    public NetworkImpl(NetworkComponentRegistry networkComponentRegistry) {
        components = networkComponentRegistry.buildComponentMap(this);
    }

    @Override
    public <T extends NetworkComponent> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }

    @Override
    public void addContainer(NetworkNodeContainer<?> container) {
        components.values().forEach(c -> c.onContainerAdded(container));
    }

    @Override
    public void removeContainer(NetworkNodeContainer<?> container) {
        components.values().forEach(c -> c.onContainerRemoved(container));
    }

    @Override
    public void remove() {
        components.values().forEach(NetworkComponent::onNetworkRemoved);
    }

    @Override
    public void split(Set<Network> networks) {
        components.values().forEach(c -> c.onNetworkSplit(networks));
    }

    @Override
    public void merge(Network network) {
        components.values().forEach(c -> c.onNetworkMerge(network));
    }
}
