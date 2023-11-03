package com.refinedmods.refinedstorage2.platform.api.support.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FuzzyModeNormalizer;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface FuzzyResourceList<T extends FuzzyModeNormalizer<T>> extends ResourceList<T> {
    /**
     * Retrieves all resources that match the normalized variant from {@link FuzzyModeNormalizer}.
     *
     * @param resource the resource, doesn't matter if it's normalized or not
     * @return a list of fuzzy matched variants, or empty list if none found
     */
    Collection<ResourceAmount<T>> getFuzzy(T resource);
}
