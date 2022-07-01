package com.refinedmods.refinedstorage2.api.core;

import java.util.Collection;
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

    public static void validateNonNegative(final long value, final String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateLargerThanZero(final long value, final String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void validateEmpty(final Collection<T> collection, final String message) {
        if (!collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void validateContains(final Collection<T> collection, final T value, final String message) {
        if (!collection.contains(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
