package com.refinedmods.refinedstorage.common.storage;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record ErrorHandlingMapCodec<K, V>(
    Codec<K> keyCodec,
    Codec<V> elementCodec
) implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingMapCodec.class);
    private static final String ERROR_MESSAGE = "Refined Storage could not load a storage. Error message:";

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input)
            .setLifecycle(Lifecycle.stable())
            .flatMap(map -> safeDecode(ops, map))
            .map(r -> Pair.of(r, input));
    }

    private <T> DataResult<Map<K, V>> safeDecode(final DynamicOps<T> ops, final MapLike<T> input) {
        final Object2ObjectMap<K, V> read = new Object2ObjectArrayMap<>();
        input.entries().forEach(entry -> {
            final DataResult<K> key = keyCodec().parse(ops, entry.getFirst());
            final DataResult<V> value = elementCodec().parse(ops, entry.getSecond());
            final DataResult<Pair<K, V>> entryResult = key.apply2stable(Pair::of, value);
            entryResult.resultOrPartial().ifPresent(pair -> read.putIfAbsent(pair.getFirst(), pair.getSecond()));
            entryResult.error().ifPresent(e -> LOGGER.warn("{} {}", ERROR_MESSAGE, e.message()));
        });
        final Map<K, V> elements = ImmutableMap.copyOf(read);
        return DataResult.success(elements);
    }

    @Override
    public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public String toString() {
        return "ErrorHandlingMapCodec[" + keyCodec + " -> " + elementCodec + ']';
    }
}

