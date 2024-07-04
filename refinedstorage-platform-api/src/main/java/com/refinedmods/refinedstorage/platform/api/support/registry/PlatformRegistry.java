package com.refinedmods.refinedstorage.platform.api.support.registry;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

/**
 * @param <T> the value type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface PlatformRegistry<T> {
    /**
     * Registers a value in the registry, identified by the id.
     * Duplicate IDs or values are not allowed.
     *
     * @param id    the id
     * @param value the value
     */
    void register(ResourceLocation id, T value);

    /**
     * @param value the value
     * @return the id of the value, if present
     */
    Optional<ResourceLocation> getId(T value);

    /**
     * @param id the id
     * @return the value, if present
     */
    Optional<T> get(ResourceLocation id);

    /**
     * @return an unmodifiable list of all values
     */
    List<T> getAll();

    /**
     * Returns the next value in the ordered list.
     * If the value is not found, it will return the first value.
     * If the value is the last value in the ordered list, it will return null.
     *
     * @param value the given value
     * @return the next value after the given value, or null if it's the last value
     */
    @Nullable
    T nextOrNullIfLast(T value);

    /**
     * @return a {@link Codec} for this registry
     */
    Codec<T> codec();

    /**
     * @return a {@link StreamCodec} for this registry
     */
    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
