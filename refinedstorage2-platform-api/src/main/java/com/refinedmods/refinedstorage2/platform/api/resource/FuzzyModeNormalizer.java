package com.refinedmods.refinedstorage2.platform.api.resource;

public interface FuzzyModeNormalizer<T> {
    T normalize();

    static Object tryNormalize(boolean fuzzyMode, Object value) {
        if (!fuzzyMode) {
            return value;
        }
        if (value instanceof FuzzyModeNormalizer<?> normalizer) {
            return normalizer.normalize();
        }
        return value;
    }
}
