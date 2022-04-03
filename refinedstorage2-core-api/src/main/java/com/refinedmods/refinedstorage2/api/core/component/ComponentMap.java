package com.refinedmods.refinedstorage2.api.core.component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class ComponentMap<C> implements ComponentAccessor<C> {
    private final Map<Class<? extends C>, C> map;

    public ComponentMap(LinkedHashMap<Class<? extends C>, C> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Collection<C> getComponents() {
        return map.values();
    }

    @Override
    public <C2 extends C> C2 getComponent(Class<C2> componentType) {
        return (C2) map.get(componentType);
    }
}
