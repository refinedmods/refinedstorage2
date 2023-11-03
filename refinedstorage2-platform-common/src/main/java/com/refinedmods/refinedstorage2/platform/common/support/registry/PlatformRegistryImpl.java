package com.refinedmods.refinedstorage2.platform.common.support.registry;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public class PlatformRegistryImpl<T> implements PlatformRegistry<T> {
    private static final String VALUE_NOT_PRESENT_ERROR = "Value must be present";
    private static final String ID_NOT_PRESENT_ERROR = "ID must be present";

    private final Map<ResourceLocation, T> idToValueMap = new HashMap<>();
    private final Map<T, ResourceLocation> valueToIdMap = new HashMap<>();
    private final List<T> order = new ArrayList<>();
    private final T defaultValue;

    public PlatformRegistryImpl(final ResourceLocation defaultValueId, final T defaultValue) {
        this.register(defaultValueId, defaultValue);
        this.defaultValue = defaultValue;
    }

    @Override
    public void register(final ResourceLocation id, final T value) {
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
    public Optional<ResourceLocation> getId(final T value) {
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        return Optional.ofNullable(valueToIdMap.get(value));
    }

    @Override
    public Optional<T> get(final ResourceLocation id) {
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
        final T nextValue = nextOrNullIfLast(value);
        if (nextValue == null) {
            return order.get(0);
        }
        return nextValue;
    }

    @Nullable
    @Override
    public T nextOrNullIfLast(final T value) {
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        final int index = order.indexOf(value);
        final int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return null;
        }
        return order.get(nextIndex);
    }
}
