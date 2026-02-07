package com.refinedmods.refinedstorage.common.support.containermenu;

import java.util.function.Function;

import net.minecraft.resources.Identifier;

public record PropertyType<T>(Identifier id,
                              Function<T, Integer> serializer,
                              Function<Integer, T> deserializer) {
}
