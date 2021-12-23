package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class NetworkComponentRegistryImpl implements NetworkComponentRegistry {
    private final LinkedHashMap<Class<? extends NetworkComponent>, Function<Network, NetworkComponent>> registry = new LinkedHashMap<>();

    @Override
    public void addComponent(Class<? extends NetworkComponent> clazz, Function<Network, NetworkComponent> factory) {
        registry.put(clazz, factory);
    }

    @Override
    public LinkedHashMap<Class<? extends NetworkComponent>, NetworkComponent> buildComponentMap(Network network) {
        LinkedHashMap<Class<? extends NetworkComponent>, NetworkComponent> components = new LinkedHashMap<>();
        registry.forEach((clazz, factory) -> components.put(clazz, factory.apply(network)));
        return components;
    }
}
