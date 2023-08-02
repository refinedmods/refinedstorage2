package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class ColorMap<T> {
    protected final DyeColor defaultColor;
    private final Map<DyeColor, Supplier<T>> map = new EnumMap<>(DyeColor.class);
    private final ResourceLocation baseId;

    public ColorMap(final ResourceLocation baseId, final DyeColor defaultColor) {
        this.baseId = Objects.requireNonNull(baseId);
        this.defaultColor = Objects.requireNonNull(defaultColor);
    }

    public boolean isDefaultColor(final DyeColor color) {
        return defaultColor == color;
    }

    protected final void putAll(final Function<DyeColor, Supplier<T>> factory) {
        for (final DyeColor color : DyeColor.values()) {
            map.put(color, factory.apply(color));
        }
    }

    protected final ResourceLocation getId(final DyeColor color) {
        if (color == defaultColor) {
            return baseId;
        }
        return new ResourceLocation(baseId.getNamespace(), color.getName() + "_" + baseId.getPath());
    }

    public void forEach(final ColoredConsumer<T> consumer) {
        map.entrySet().stream().sorted(new ColoredSorter<>(defaultColor)).forEach(
            entry -> consumer.accept(entry.getKey(), getId(entry.getKey()), entry.getValue())
        );
    }

    public T get(final DyeColor color) {
        return map.get(color).get();
    }

    public T getDefault() {
        return get(defaultColor);
    }

    public Collection<T> values() {
        return map.values().stream().map(Supplier::get).toList();
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

    @FunctionalInterface
    public interface ColoredConsumer<T> {
        void accept(DyeColor color, ResourceLocation id, Supplier<T> supplier);
    }
}
