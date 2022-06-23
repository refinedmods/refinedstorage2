package com.refinedmods.refinedstorage2.api.core.registry;

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Represents a registry that keeps track of the order of registered values.
 *
 * @param <I> the identifier type
 * @param <T> the value type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface OrderedRegistry<I, T> {
    /**
     * Registers a value in the registry, identified by the id.
     * Duplicate IDs or values are not allowed.
     *
     * @param id    the id
     * @param value the value
     */
    void register(I id, T value);

    /**
     * @return whether if there is any other value, ignoring any default value
     */
    boolean isEmpty();

    /**
     * @param value the value
     * @return the id of the value, if present
     */
    Optional<I> getId(T value);

    /**
     * @param id the id
     * @return the value, if present
     */
    Optional<T> get(I id);

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
     * @param value the value
     * @return the next value after the given value
     */
    T next(T value);
}
