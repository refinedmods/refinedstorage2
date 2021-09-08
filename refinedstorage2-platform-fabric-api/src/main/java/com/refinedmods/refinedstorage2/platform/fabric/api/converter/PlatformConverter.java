package com.refinedmods.refinedstorage2.platform.fabric.api.converter;

public interface PlatformConverter<P, T> {
    P toPlatform(T value);

    T toDomain(P value);
}
