package com.refinedmods.refinedstorage2.platform.common.containermenu.property;

import com.refinedmods.refinedstorage2.platform.common.Platform;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.inventory.DataSlot;

public class TwoWaySyncProperty<T> extends DataSlot {
    private final int index;
    private final Function<T, Integer> serializer;
    private final Function<Integer, T> deserializer;
    @Nullable
    private final Supplier<T> serverSupplier;
    private final Consumer<T> changed;
    private T value;

    private TwoWaySyncProperty(final int index,
                               final Function<T, Integer> serializer,
                               final Function<Integer, T> deserializer,
                               final @Nullable Supplier<T> serverSupplier,
                               final Consumer<T> changed,
                               final T defaultValue) {
        this.index = index;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.serverSupplier = serverSupplier;
        this.changed = changed;
        this.value = defaultValue;
    }

    public static <T> TwoWaySyncProperty<T> forClient(final int index,
                                                      final Function<T, Integer> serializer,
                                                      final Function<Integer, T> deserializer,
                                                      final T defaultValue,
                                                      final Consumer<T> changed) {
        return new TwoWaySyncProperty<>(index, serializer, deserializer, null, changed, defaultValue);
    }

    public static TwoWaySyncProperty<Integer> integerForClient(final int index) {
        return TwoWaySyncProperty.forClient(
                index,
                value -> value,
                value -> value,
                0,
                value -> {
                }
        );
    }

    public static TwoWaySyncProperty<Boolean> booleanForClient(final int index) {
        return TwoWaySyncProperty.forClient(
                index,
                value -> Boolean.TRUE.equals(value) ? 0 : 1,
                value -> value == 0,
                true,
                value -> {
                }
        );
    }

    public static <T> TwoWaySyncProperty<T> forServer(final int index,
                                                      final Function<T, Integer> serializer,
                                                      final Function<Integer, T> deserializer,
                                                      final Supplier<T> supplier,
                                                      final Consumer<T> changed) {
        return new TwoWaySyncProperty<>(index, serializer, deserializer, supplier, changed, supplier.get());
    }

    public void syncToServer(final T newValue) {
        Platform.INSTANCE.getClientToServerCommunications().sendPropertyChange(index, serializer.apply(newValue));
    }

    public T getDeserialized() {
        return value;
    }

    @Override
    public int get() {
        return serverSupplier != null ? serializer.apply(serverSupplier.get()) : serializer.apply(value);
    }

    @Override
    public void set(int value) {
        final T deserializedValue = deserializer.apply(value);
        changed.accept(deserializedValue);
        this.value = deserializedValue;
    }
}
