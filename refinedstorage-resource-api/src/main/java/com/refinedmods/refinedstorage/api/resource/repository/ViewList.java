package com.refinedmods.refinedstorage.api.resource.repository;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

class ViewList<T> {
    private final List<T> list = new ArrayList<>();
    private final List<T> listView = Collections.unmodifiableList(list);
    private final Map<ResourceKey, T> index = new HashMap<>();

    @Nullable
    T get(final ResourceKey resource) {
        return index.get(resource);
    }

    void remove(final ResourceKey resource, final T mapped) {
        list.remove(mapped);
        index.remove(resource);
    }

    void add(final ResourceKey resource, final T mapped, final Comparator<T> sort) {
        index.put(resource, mapped);
        add(mapped, sort);
    }

    private void add(final T mapped, final Comparator<T> sort) {
        // Calculate the position according to sorting rules.
        final int wouldBePosition = Collections.binarySearch(list, mapped, sort);
        // Most of the time, the "would be" position is negative, indicating that the resource wasn't found yet in the
        // list, comparing with sorting rules. The absolute of this position would be the "real" position if sorted.
        if (wouldBePosition < 0) {
            list.add(-wouldBePosition - 1, mapped);
        } else {
            // If the "would-be" position is positive, this means that the resource is already contained in the list,
            // comparing with sorting rules.
            // This doesn't mean that the *exact* resource is already in the list, but that is purely "contained"
            // in the list when comparing with sorting rules.
            // For example: a resource with different identity but the same name (in Minecraft: an enchanted book
            // with different NBT).
            // In that case, just insert it after the "existing" resource.
            list.add(wouldBePosition + 1, mapped);
        }
    }

    void update(final T mapped, final Comparator<T> sort) {
        list.remove(mapped);
        add(mapped, sort);
    }

    List<T> getListView() {
        return listView;
    }

    void clear() {
        index.clear();
        list.clear();
    }

    static <T> ViewList<T> createSorted(final ResourceList source,
                                        final Set<ResourceKey> stickyResources,
                                        final Comparator<T> sort,
                                        final ResourceRepositoryMapper<T> mapper,
                                        final Predicate<T> filter) {
        final ViewList<T> list = new ViewList<>();
        for (final ResourceKey resource : source.getAll()) {
            tryAdd(resource, list, mapper, filter);
        }
        for (final ResourceKey stickyResource : stickyResources) {
            if (!list.index.containsKey(stickyResource)) {
                tryAdd(stickyResource, list, mapper, filter);
            }
        }
        list.list.sort(sort);
        return list;
    }

    private static <T> void tryAdd(final ResourceKey resource,
                                   final ViewList<T> list,
                                   final ResourceRepositoryMapper<T> mapper,
                                   final Predicate<T> filter) {
        final T mapped = mapper.apply(resource);
        tryAdd(mapped, list, resource, filter);
    }

    private static <T> void tryAdd(final T mapped,
                                   final ViewList<T> newList,
                                   final ResourceKey resource,
                                   final Predicate<T> filter) {
        if (filter.test(mapped)) {
            newList.list.add(mapped);
            newList.index.put(resource, mapped);
        }
    }
}
