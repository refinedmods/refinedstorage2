package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class ColorMap<T> {
    private static final DyeColor NORMAL_COLOR = DyeColor.LIGHT_BLUE;

    private final Map<DyeColor, Supplier<T>> map = new EnumMap<>(DyeColor.class);

    public void putAll(final Function<DyeColor, Supplier<T>> factory) {
        for (final DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color));
        }
    }

    public ResourceLocation getId(final DyeColor color, final ResourceLocation id) {
        if (color == NORMAL_COLOR) {
            return id;
        }
        return new ResourceLocation(id.getNamespace(), color.getSerializedName() + "_" + id.getPath());
    }

    public MutableComponent getName(final DyeColor color, final MutableComponent name) {
        if (color != NORMAL_COLOR) {
            return Component.translatable("color.minecraft." + color.getName()).append(" ").append(name);
        } else {
            return name;
        }
    }

    public void forEach(final BiConsumer<DyeColor, Supplier<T>> consumer) {
        map.forEach(consumer);
    }

    public T get(final DyeColor color) {
        return map.get(color).get();
    }

    public T getNormal() {
        return get(NORMAL_COLOR);
    }

    public Collection<T> values() {
        return map.values().stream().map(Supplier::get).collect(Collectors.toSet());
    }
}
