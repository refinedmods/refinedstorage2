package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;

public record PropertyType<T>(ResourceLocation id,
                              Function<T, Integer> serializer,
                              Function<Integer, T> deserializer) {
}
