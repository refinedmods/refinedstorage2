package com.refinedmods.refinedstorage.common.content;

import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public record DirectRegistryCallback<T>(Registry<T> registry) implements RegistryCallback<T> {
    @Override
    public <R extends T> Supplier<R> register(final Identifier id, final Supplier<R> value) {
        final R result = Registry.register(registry, id, value.get());
        return () -> result;
    }
}
