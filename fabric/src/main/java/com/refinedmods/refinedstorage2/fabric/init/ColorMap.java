package com.refinedmods.refinedstorage2.fabric.init;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.util.DyeColor;
import org.apache.logging.log4j.util.TriConsumer;

public class ColorMap<T> {
    protected final Map<DyeColor, T> map = new EnumMap<>(DyeColor.class);

    public void putAll(BiFunction<DyeColor, Function<String, String>, T> factory) {
        for (DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color, name -> getName(color, name)));
        }
    }

    private String getName(DyeColor color, String name) {
        if (color == DyeColor.LIGHT_BLUE) {
            return name;
        }
        return color.asString() + "_" + name;
    }

    public void forEach(TriConsumer<DyeColor, T, Function<String, String>> consumer) {
        map.forEach((color, obj) -> consumer.accept(color, obj, name -> getName(color, name)));
    }

    public Collection<T> values() {
        return map.values();
    }
}
