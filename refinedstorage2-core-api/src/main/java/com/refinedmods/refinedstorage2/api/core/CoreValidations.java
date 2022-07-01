package com.refinedmods.refinedstorage2.api.core;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public final class CoreValidations {
    private CoreValidations() {
    }

    public static <T> void validateNotNull(@Nullable final T value, final String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }
}
