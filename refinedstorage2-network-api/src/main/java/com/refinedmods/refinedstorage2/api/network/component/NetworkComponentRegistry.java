package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;

import java.util.LinkedHashMap;
import java.util.function.Function;

public interface NetworkComponentRegistry {
    void addComponent(Class<? extends NetworkComponent> clazz, Function<Network, NetworkComponent> factory);

    LinkedHashMap<Class<? extends NetworkComponent>, NetworkComponent> buildComponentMap(Network network);
}
