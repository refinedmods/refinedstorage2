package com.refinedmods.refinedstorage.api.network.node;

import java.util.HashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Accumulates the details of multiple {@link NetworkNodeDetailsProvider}s.
 * Every provider merges its details into the {@link MergedNetworkNodeDetailsElement}s that it knows about.
 */
@API(status = API.Status.STABLE, since = "3.3.0")
public final class MergedNetworkNodeDetails {
    private final Map<MergedNetworkNodeDetailsElement<?>, Object> elements = new HashMap<>();

    /**
     * Retrieves the state of an element, creating it if no node has merged into it yet.
     *
     * @param element the element
     * @param <T>     the type of the state
     * @return the state
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(final MergedNetworkNodeDetailsElement<T> element) {
        return (T) elements.computeIfAbsent(element, MergedNetworkNodeDetailsElement::create);
    }

    /**
     * @param element the element
     * @param <T>     the type of the state
     * @return the state, or {@code null} if no node has merged into the element
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(final MergedNetworkNodeDetailsElement<T> element) {
        return (T) elements.get(element);
    }
}
