package com.refinedmods.refinedstorage2.api.grid.service;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridExtractMode {
    /**
     * Will try to extract the entire resource, depending on the maximum count of said resource.
     */
    ENTIRE_RESOURCE,
    /**
     * Will try to extract half of an entire resource, depending on the maximum count of said resource.
     */
    HALF_RESOURCE,
    /**
     * Will try to extract a single resource.
     */
    SINGLE_RESOURCE
}
