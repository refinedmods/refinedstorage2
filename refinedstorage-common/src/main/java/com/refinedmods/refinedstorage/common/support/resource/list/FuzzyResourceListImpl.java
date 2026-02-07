package com.refinedmods.refinedstorage.common.support.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.AbstractProxyMutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.list.FuzzyResourceList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public class FuzzyResourceListImpl extends AbstractProxyMutableResourceList implements FuzzyResourceList {
    private final Map<ResourceKey, Set<ResourceKey>> normalizedFuzzyMap = new HashMap<>();

    public FuzzyResourceListImpl(final MutableResourceList delegate) {
        super(delegate);
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        final OperationResult result = super.add(resource, amount);
        addToIndex(resource, result);
        return result;
    }

    private void addToIndex(final ResourceKey resource, final OperationResult result) {
        if (resource instanceof FuzzyModeNormalizer normalizer) {
            normalizedFuzzyMap.computeIfAbsent(normalizer.normalize(), k -> new HashSet<>()).add(result.resource());
        }
    }

    @Override
    @Nullable
    public OperationResult remove(final ResourceKey resource, final long amount) {
        final OperationResult result = super.remove(resource, amount);
        if (result != null && !result.available()) {
            removeFromIndex(resource, result);
        }
        return result;
    }

    private void removeFromIndex(final ResourceKey resource, final OperationResult result) {
        if (!(resource instanceof FuzzyModeNormalizer normalizer)) {
            return;
        }
        final ResourceKey normalized = normalizer.normalize();
        final Collection<ResourceKey> index = normalizedFuzzyMap.get(normalized);
        if (index == null) {
            return;
        }
        index.remove(result.resource());
        if (index.isEmpty()) {
            normalizedFuzzyMap.remove(normalized);
        }
    }

    @Override
    public Collection<ResourceKey> getFuzzy(final ResourceKey resource) {
        if (resource instanceof FuzzyModeNormalizer normalizer) {
            return Collections.unmodifiableCollection(
                normalizedFuzzyMap.getOrDefault(normalizer.normalize(), Collections.emptySet())
            );
        }
        return Collections.unmodifiableCollection(
            normalizedFuzzyMap.getOrDefault(resource, Collections.emptySet())
        );
    }
}
