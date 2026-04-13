package com.refinedmods.refinedstorage.common.content;

import java.util.function.Supplier;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface RegistryCallback<T> {
    <R extends T> Supplier<R> register(Identifier id, Supplier<R> value);
}
