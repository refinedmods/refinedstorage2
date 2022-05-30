package com.refinedmods.refinedstorage2.api.core.component;

import java.util.LinkedHashMap;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class ComponentMapFactory<C, X> {
    private final LinkedHashMap<Class<? extends C>, Function<X, C>> factories;

    public ComponentMapFactory() {
        this(new LinkedHashMap<>());
    }

    private ComponentMapFactory(LinkedHashMap<Class<? extends C>, Function<X, C>> factories) {
        this.factories = factories;
    }

    public void addFactory(Class<? extends C> componentType, Function<X, C> factory) {
        factories.put(componentType, factory);
    }

    public ComponentMapFactory<C, X> copy() {
        return new ComponentMapFactory<>(new LinkedHashMap<>(factories));
    }

    public ComponentMap<C> buildComponentMap(X context) {
        LinkedHashMap<Class<? extends C>, C> components = new LinkedHashMap<>();
        factories.forEach((componentType, factory) -> components.put(componentType, factory.apply(context)));
        return new ComponentMap<>(components);
    }
}
