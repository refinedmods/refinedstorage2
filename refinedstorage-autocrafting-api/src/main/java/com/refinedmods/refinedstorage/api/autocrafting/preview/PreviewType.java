package com.refinedmods.refinedstorage.api.autocrafting.preview;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public enum PreviewType {
    SUCCESS,
    MISSING_RESOURCES,
    CYCLE_DETECTED,
    OVERFLOW,
    CANCELLED
}
