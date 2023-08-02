package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface RegistryCallback<T> {
    <R extends T> Supplier<R> register(ResourceLocation id, Supplier<R> value);
}
