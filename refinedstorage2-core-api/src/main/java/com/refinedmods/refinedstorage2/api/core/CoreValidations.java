package com.refinedmods.refinedstorage2.api.core;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public final class CoreValidations {
    private CoreValidations() {
    }

    public static <T> void validateEquals(@Nullable final T a, @Nullable final T b, final String message) {
        if (!Objects.equals(a, b)) {
            throw new IllegalStateException(message);
        }
    }

    public static <T> T validateNotNull(@Nullable final T value, final String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static void validateNegative(final long value, final String message) {
        if (value >= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static int validateNotNegative(final int value, final String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static long validateNotNegative(final long value, final String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
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
