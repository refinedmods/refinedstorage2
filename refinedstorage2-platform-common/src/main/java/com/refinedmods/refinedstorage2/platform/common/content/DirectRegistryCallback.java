package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record DirectRegistryCallback<T>(Registry<T> registry) implements RegistryCallback<T> {
    @Override
    public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
        final R result = Registry.register(registry, id, value.get());
        return () -> result;
    }
}
