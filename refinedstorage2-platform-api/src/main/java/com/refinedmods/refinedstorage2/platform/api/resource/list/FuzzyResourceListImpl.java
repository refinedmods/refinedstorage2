package com.refinedmods.refinedstorage2.platform.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.AbstractProxyResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class FuzzyResourceListImpl<T extends FuzzyModeNormalizer<T>>
    extends AbstractProxyResourceList<T>
    implements FuzzyResourceList<T> {
    private final Map<T, Set<ResourceAmount<T>>> normalizedFuzzyMap = new HashMap<>();

    public FuzzyResourceListImpl(final ResourceList<T> delegate) {
        super(delegate);
    }

    @Override
    public ResourceListOperationResult<T> add(final T resource, final long amount) {
        final ResourceListOperationResult<T> result = super.add(resource, amount);
        addToIndex(resource, result);
        return result;
    }

    private void addToIndex(final T resource, final ResourceListOperationResult<T> result) {
        normalizedFuzzyMap.computeIfAbsent(resource.normalize(), k -> new HashSet<>()).add(result.resourceAmount());
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(final T resource, final long amount) {
        return super.remove(resource, amount)
            .map(result -> {
                if (!result.available()) {
                    removeFromIndex(resource, result);
                }
                return result;
            });
    }

    private void removeFromIndex(final T resource, final ResourceListOperationResult<T> result) {
        final T normalized = resource.normalize();
        final Collection<ResourceAmount<T>> index = normalizedFuzzyMap.get(normalized);
        if (index == null) {
            return;
        }
        index.remove(result.resourceAmount());
        if (index.isEmpty()) {
            normalizedFuzzyMap.remove(normalized);
        }
    }

    @Override
    public Collection<ResourceAmount<T>> getFuzzy(final T resource) {
        return normalizedFuzzyMap.getOrDefault(resource.normalize(), Collections.emptySet());
    }
}
