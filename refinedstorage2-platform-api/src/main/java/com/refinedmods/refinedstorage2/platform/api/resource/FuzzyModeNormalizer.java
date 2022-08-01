package com.refinedmods.refinedstorage2.platform.api.resource;

public interface FuzzyModeNormalizer<T> {
    T normalize();

    static Object tryNormalize(boolean exactMode, Object value) {
        if (exactMode) {
            return value;
        }
        if (value instanceof FuzzyModeNormalizer<?> normalizer) {
            return normalizer.normalize();
        }
        return value;
    }
}
