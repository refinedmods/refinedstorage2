package com.refinedmods.refinedstorage.common.support.registry;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class PlatformRegistryImpl<T> implements PlatformRegistry<T> {
    private static final String VALUE_NOT_PRESENT_ERROR = "Value must be present";
    private static final String ID_NOT_PRESENT_ERROR = "ID must be present";

    private final Map<Identifier, T> idToValueMap = new HashMap<>();
    private final Map<T, Identifier> valueToIdMap = new HashMap<>();
    private final List<T> order = new ArrayList<>();
    private final List<T> viewList = Collections.unmodifiableList(order);
    private final Codec<T> codec = Identifier.CODEC.comapFlatMap(
        id -> get(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown ID: " + id)),
        value -> getId(value).orElseThrow()
    );
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = new StreamCodec<>() {
        @Override
        public T decode(final RegistryFriendlyByteBuf buf) {
            return get(buf.readIdentifier()).orElseThrow();
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final T value) {
            buf.writeIdentifier(getId(value).orElseThrow());
        }
    };

    @Override
    public void register(final Identifier id, final T value) {
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
    public Optional<Identifier> getId(final T value) {
        CoreValidations.validateNotNull(value, VALUE_NOT_PRESENT_ERROR);
        return Optional.ofNullable(valueToIdMap.get(value));
    }

    @Override
    public Optional<T> get(final Identifier id) {
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
