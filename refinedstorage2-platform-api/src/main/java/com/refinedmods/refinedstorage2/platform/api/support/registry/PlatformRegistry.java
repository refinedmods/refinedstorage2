package com.refinedmods.refinedstorage2.platform.api.support.registry;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

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
     * @return whether if there is any other value, ignoring any default value
     */
    boolean isEmpty();

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
     * @return the default value
     */
    T getDefault();

    /**
     * @return an unmodifiable list of all values
     */
    List<T> getAll();

    /**
     * Returns the next value in the ordered list.
     * If the value is not found, it will return the default value.
     *
     * @param value the given value
     * @return the next value after the given value
     */
    T next(T value);

    /**
     * Returns the next value in the ordered list.
     * If the value is not found, it will the default value.
     * If the value is the last value in the ordered list, it will return null.
     *
     * @param value the given value
     * @return the next value after the given value, or null if it's the last value
     */
    @Nullable
    T nextOrNullIfLast(T value);
}
