package com.refinedmods.refinedstorage.api.resource.repository;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.api.resource.repository.ViewList.createSorted;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class ResourceRepositoryImpl<T> implements ResourceRepository<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRepositoryImpl.class);

    private final MutableResourceList backingList;
    private final Comparator<T> identitySort;
    private final ResourceRepositoryMapper<T> mapper;
    private final Set<ResourceKey> stickyResources;

    private ViewList<T> viewList = new ViewList<>();
    private Comparator<T> sort;
    private ResourceRepositoryFilter<T> filter = (view, resource) -> true;
    @Nullable
    private Runnable listener;
    private boolean preventSorting;

    /**
     * @param mapper              a mapper that transforms resources to the representation in the view list
     * @param backingList         the backing list
     * @param stickyResources     resources which must stay in the view list
     * @param identitySortingType a sorting type required to keep a consistent sorting order with quantity sorting
     * @param defaultSortingType  the default sorting type
     */
    public ResourceRepositoryImpl(final ResourceRepositoryMapper<T> mapper,
                                  final MutableResourceList backingList,
                                  final Set<ResourceKey> stickyResources,
                                  final Function<ResourceRepository<T>, Comparator<T>> identitySortingType,
                                  final Function<ResourceRepository<T>, Comparator<T>> defaultSortingType) {
        this.mapper = new CachingResourceRepositoryMapper<>(mapper);
        this.identitySort = identitySortingType.apply(this);
        this.sort = createSort(defaultSortingType.apply(this), identitySort, SortingDirection.ASCENDING);
        this.backingList = backingList;
        this.stickyResources = stickyResources;
    }

    @Override
    public void setListener(@Nullable final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void setSort(final Comparator<T> theSort, final SortingDirection direction) {
        this.sort = createSort(theSort, identitySort, direction);
    }

    @Override
    public ResourceRepositoryFilter<T> setFilterAndSort(final ResourceRepositoryFilter<T> theFilter) {
        final ResourceRepositoryFilter<T> previousFilter = this.filter;
        this.filter = theFilter;
        sort();
        return previousFilter;
    }

    @Override
    public boolean setPreventSorting(final boolean thePreventSorting) {
        final boolean changed = this.preventSorting != thePreventSorting;
        this.preventSorting = thePreventSorting;
        return changed;
    }

    @Override
    public long getAmount(final ResourceKey resource) {
        return backingList.get(resource);
    }

    @Override
    public boolean isSticky(final ResourceKey resource) {
        return stickyResources.contains(resource);
    }

    @Override
    public void sort() {
        LOGGER.debug("Sorting resource repository");
        viewList = createSorted(backingList, stickyResources, sort, mapper, resource -> filter.test(this, resource));
        notifyListener();
    }

    @Override
    public void update(final ResourceKey resource, final long amount) {
        if (amount == 0) {
            throw new IllegalArgumentException("Amount must be non-zero");
        }
        final MutableResourceList.OperationResult backingListResult = updateBackingList(resource, amount);
        if (backingListResult == null) {
            LOGGER.warn("Failed to update backing list for {} {}", amount, resource);
            return;
        }
        final T mapped = viewList.get(resource);
        if (mapped != null) {
            updateExisting(resource, !backingListResult.available(), mapped);
            return;
        }
        tryAddNewResource(resource);
    }

    private MutableResourceList.@Nullable OperationResult updateBackingList(final ResourceKey resource,
                                                                            final long amount) {
        if (amount < 0) {
            return backingList.remove(resource, Math.abs(amount));
        }
        return backingList.add(resource, amount);
    }

    private void updateExisting(final ResourceKey resource,
                                final boolean removedFromBackingList,
                                final T mapped) {
        final boolean canBeSorted = !preventSorting;
        if (canBeSorted) {
            LOGGER.debug("Actually updating {} resource in the view list", resource);
            if (removedFromBackingList && !stickyResources.contains(resource)) {
                viewList.remove(resource, mapped);
                notifyListener();
            } else {
                viewList.update(mapped, sort);
                notifyListener();
            }
        } else if (removedFromBackingList) {
            LOGGER.debug("{} is no longer available", resource);
        } else {
            LOGGER.debug("{} can't be sorted, preventing sorting is on", resource);
        }
    }

    private void tryAddNewResource(final ResourceKey resource) {
        final T mapped = mapper.apply(resource);
        if (filter.test(this, mapped)) {
            LOGGER.debug("Filter allowed, actually adding {}", resource);
            viewList.add(resource, mapped, sort);
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    @Override
    public List<T> getViewList() {
        return viewList.getListView();
    }

    @Override
    public MutableResourceList copyBackingList() {
        return backingList.copy();
    }

    @Override
    public void clear() {
        backingList.clear();
        viewList.clear();
    }

    private static <T> Comparator<T> createSort(final Comparator<T> sort,
                                                final Comparator<T> identitySort,
                                                final SortingDirection direction) {
        // An identity sort is necessary so the order of items is preserved in quantity sorting mode.
        // If two grid resources have the same quantity, their order would otherwise not be preserved.
        final Comparator<T> comparator = sort.thenComparing(identitySort);
        if (direction == SortingDirection.ASCENDING) {
            return comparator;
        }
        return comparator.reversed();
    }
}
