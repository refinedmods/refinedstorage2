package com.refinedmods.refinedstorage2.api.core.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public class OrderedRegistryImpl<I, T> implements OrderedRegistry<I, T> {
    private static final String VALUE_NOT_PRESENT_ERROR = "Value must be present";
    private static final String ID_NOT_PRESENT_ERROR = "ID must be present";

    private final Map<I, T> idToValueMap = new HashMap<>();
    private final Map<T, I> valueToIdMap = new HashMap<>();
    private final List<T> order = new ArrayList<>();
    private final T defaultValue;

    public OrderedRegistryImpl(final I defaultValueId, final T defaultValue) {
        this.register(defaultValueId, defaultValue);
        this.defaultValue = defaultValue;
    }

    @Override
    public void register(final I id, final T value) {
        Preconditions.checkNotNull(id, ID_NOT_PRESENT_ERROR);
        Preconditions.checkNotNull(value, VALUE_NOT_PRESENT_ERROR);
        if (idToValueMap.containsKey(id) || order.contains(value)) {
            throw new IllegalArgumentException("Already registered");
        }
        idToValueMap.put(id, value);
        valueToIdMap.put(value, id);
        order.add(value);
    }

    @Override
    public boolean isEmpty() {
        return order.size() == 1;
    }

    @Override
    public Optional<I> getId(final T value) {
        Preconditions.checkNotNull(value, VALUE_NOT_PRESENT_ERROR);
        return Optional.ofNullable(valueToIdMap.get(value));
    }

    @Override
    public Optional<T> get(final I id) {
        Preconditions.checkNotNull(id, ID_NOT_PRESENT_ERROR);
        return Optional.ofNullable(idToValueMap.get(id));
    }

    @Override
    public T getDefault() {
        return defaultValue;
    }

    @Override
    public List<T> getAll() {
        return List.copyOf(order);
    }

    @Override
    public T next(final T value) {
        Preconditions.checkNotNull(value, VALUE_NOT_PRESENT_ERROR);
        int index = order.indexOf(value);
        int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return order.get(0);
        }
        return order.get(nextIndex);
    }
}
