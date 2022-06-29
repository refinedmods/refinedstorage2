package com.refinedmods.refinedstorage2.api.core.component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class ComponentMapFactory<C, X> {
    private final Map<Class<? extends C>, Function<X, C>> factories;

    public ComponentMapFactory() {
        this(new LinkedHashMap<>());
    }

    private ComponentMapFactory(final Map<Class<? extends C>, Function<X, C>> factories) {
        this.factories = factories;
    }

    public void addFactory(final Class<? extends C> componentType, final Function<X, C> factory) {
        factories.put(componentType, factory);
    }

    public ComponentMapFactory<C, X> copy() {
        return new ComponentMapFactory<>(new LinkedHashMap<>(factories));
    }

    public ComponentMap<C> buildComponentMap(final X context) {
        final Map<Class<? extends C>, C> components = new LinkedHashMap<>();
        factories.forEach((componentType, factory) -> components.put(componentType, factory.apply(context)));
        return new ComponentMap<>(components);
    }
}
