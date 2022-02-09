package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class ColorMap<T> {
    private static final DyeColor NORMAL_COLOR = DyeColor.LIGHT_BLUE;

    protected final Map<DyeColor, T> map = new EnumMap<>(DyeColor.class);

    public void putAll(Function<DyeColor, T> factory) {
        for (DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color));
        }
    }

    public ResourceLocation getId(DyeColor color, ResourceLocation id) {
        if (color == NORMAL_COLOR) {
            return id;
        }
        return new ResourceLocation(id.getNamespace(), color.getSerializedName() + "_" + id.getPath());
    }

    public MutableComponent getName(DyeColor color, MutableComponent name) {
        if (color != NORMAL_COLOR) {
            return new TranslatableComponent("color.minecraft." + color.getName()).append(" ").append(name);
        } else {
            return name;
        }
    }

    public void forEach(BiConsumer<DyeColor, T> consumer) {
        map.forEach(consumer);
    }

    public T get(DyeColor color) {
        return map.get(color);
    }

    public T getNormal() {
        return get(NORMAL_COLOR);
    }

    public Collection<T> values() {
        return map.values();
    }
}
