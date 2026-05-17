package com.refinedmods.refinedstorage.common.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public abstract class AbstractGridResource<T extends PlatformResourceKey> implements GridResource {
    protected final T resource;
    private final String sortName;
    private final Set<String> searchableNames;
    private final Function<GridResourceAttributeKey, Set<String>> attributes;

    protected AbstractGridResource(final T resource,
                                   final String sortName,
                                   final Set<String> searchableNames,
                                   final Function<GridResourceAttributeKey, Set<String>> attributes) {
        this.resource = resource;
        this.sortName = sortName;
        this.searchableNames = searchableNames;
        this.attributes = attributes;
    }

    protected AbstractGridResource(final T resource, final String sortName,
                                final Function<GridResourceAttributeKey, Set<String>> attributes) {
        this(resource, sortName, Set.of(sortName), attributes);
    }

    @Override
    @Nullable
    public TrackedResource getTrackedResource(final Function<ResourceKey, TrackedResource> trackedResourceProvider) {
        return trackedResourceProvider.apply(resource);
    }

    @Override
    public long getAmount(final ResourceRepository<GridResource> repository) {
        return repository.getAmount(resource);
    }

    @Override
    public String getName() {
        return sortName;
    }

    @Override
    public Set<String> getSearchableNames() {
        return searchableNames;
    }

    @Override
    public Set<String> getAttribute(final GridResourceAttributeKey key) {
        return attributes.apply(key);
    }

    @Override
    public boolean isAutocraftable(final ResourceRepository<GridResource> repository) {
        return repository.isSticky(resource);
    }

    @Nullable
    @Override
    public PlatformResourceKey getResourceForRecipeMods() {
        return resource;
    }

    @Override
    public String toString() {
        return "AbstractGridResource{"
            + "resource=" + resource
            + ", sortName='" + sortName + '\''
            + ", searchableNames=" + searchableNames
            + ", attributes=" + attributes
            + '}';
    }
}
