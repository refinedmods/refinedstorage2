package com.refinedmods.refinedstorage.platform.common.support.registry;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.platform.api.support.registry.PlatformRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class PlatformRegistryImpl<T> implements PlatformRegistry<T> {
    private static final String VALUE_NOT_PRESENT_ERROR = "Value must be present";
    private static final String ID_NOT_PRESENT_ERROR = "ID must be present";

    private final Map<ResourceLocation, T> idToValueMap = new HashMap<>();
    private final Map<T, ResourceLocation> valueToIdMap = new HashMap<>();
    private final List<T> order = new ArrayList<>();
    private final List<T> viewList = Collections.unmodifiableList(order);
    private final Codec<T> codec = ResourceLocation.CODEC.comapFlatMap(
        id -> get(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown ID: " + id)),
        value -> getId(value).orElseThrow()
    );
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = new StreamCodec<>() {
        @Override
        public T decode(final RegistryFriendlyByteBuf buf) {
            return get(buf.readResourceLocation()).orElseThrow();
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final T value) {
            buf.writeResourceLocation(getId(value).orElseThrow());
        }
    };

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
    public List<T> getAll() {
        return viewList;
    }

    @Nullable
    @Override
    public T nextOrNullIfLast(final T value) {
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        if (order.isEmpty()) {
            return null;
        }
        final int index = order.indexOf(value);
        final int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return null;
        }
        return order.get(nextIndex);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }
}
