package com.refinedmods.refinedstorage2.api.core.registry;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        CoreValidations.validateNotNull(id, ID_NOT_PRESENT_ERROR);
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
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
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        return Optional.ofNullable(valueToIdMap.get(value));
    }

    @Override
    public Optional<T> get(final I id) {
        CoreValidations.validateNotNull(id, ID_NOT_PRESENT_ERROR);
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
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        final int index = order.indexOf(value);
        final int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return order.get(0);
        }
        return order.get(nextIndex);
    }
}
