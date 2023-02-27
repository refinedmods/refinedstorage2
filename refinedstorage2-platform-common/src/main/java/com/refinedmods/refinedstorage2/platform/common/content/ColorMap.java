package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Collection;
import java.util.Comparator;
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
    private final Map<DyeColor, Supplier<T>> map = new EnumMap<>(DyeColor.class);
    private final DyeColor defaultColor;

    public ColorMap(final DyeColor defaultColor) {
        this.defaultColor = defaultColor;
    }

    public boolean isDefaultColor(final DyeColor color) {
        return defaultColor == color;
    }

    public void putAll(final Function<DyeColor, Supplier<T>> factory) {
        for (final DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color));
        }
    }

    public ResourceLocation getId(final DyeColor color, final ResourceLocation id) {
        if (color == defaultColor) {
            return id;
        }
        return new ResourceLocation(id.getNamespace(), color.getName() + "_" + id.getPath());
    }

    public MutableComponent getName(final DyeColor color, final MutableComponent name) {
        if (color != this.defaultColor) {
            return Component.translatable("color.minecraft." + color.getName()).append(" ").append(name);
        } else {
            return name;
        }
    }

    public void forEach(final BiConsumer<DyeColor, Supplier<T>> consumer) {
        map.entrySet().stream()
            .sorted(new ColoredSorter<>(defaultColor))
            .forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }

    public T get(final DyeColor color) {
        return map.get(color).get();
    }

    public T getDefault() {
        return get(defaultColor);
    }

    public Collection<T> values() {
        return map.values().stream().map(Supplier::get).collect(Collectors.toList());
    }

    private record ColoredSorter<T>(DyeColor defaultColor) implements Comparator<Map.Entry<DyeColor, T>> {
        @Override
        public int compare(final Map.Entry<DyeColor, T> entry1, final Map.Entry<DyeColor, T> entry2) {
            return getId(entry1) - getId(entry2);
        }

        private int getId(final Map.Entry<DyeColor, T> entry) {
            return (entry.getKey().getId() - defaultColor.getId() + 16) % 16;
        }
    }
}
