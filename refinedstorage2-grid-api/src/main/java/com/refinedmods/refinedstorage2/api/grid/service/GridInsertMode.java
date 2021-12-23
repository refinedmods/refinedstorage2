package com.refinedmods.refinedstorage2.api.grid.service;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum GridInsertMode {
    /**
     * Will try to insert the entire resource.
     */
    ENTIRE_RESOURCE,
    /**
     * Will try to insert a single resource (count of 1).
     */
    SINGLE_RESOURCE
}
