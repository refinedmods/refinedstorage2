package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;

import java.util.LinkedHashMap;
import java.util.function.Function;

// TODO: Pass with a NetworkFactory instead
public interface NetworkComponentRegistry {
    NetworkComponentRegistry INSTANCE = new NetworkComponentRegistryImpl();

    void addComponent(Class<? extends NetworkComponent> clazz, Function<Network, NetworkComponent> factory);

    LinkedHashMap<Class<? extends NetworkComponent>, NetworkComponent> buildComponentMap(Network network);
}
