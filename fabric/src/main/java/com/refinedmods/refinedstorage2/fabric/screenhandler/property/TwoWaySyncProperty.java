package com.refinedmods.refinedstorage2.fabric.screenhandler.property;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.refinedmods.refinedstorage2.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.minecraft.screen.Property;

public class TwoWaySyncProperty<T> extends Property {
    private final int index;
    private final Function<T, Integer> serializer;
    private final Function<Integer, T> deserializer;
    private final Supplier<T> supplier;
    private final Consumer<T> changed;
    private T value;

    public static <T> TwoWaySyncProperty<T> forClient(int index, Function<T, Integer> serializer, Function<Integer, T> deserializer, T defaultValue, Consumer<T> changed) {
        return new TwoWaySyncProperty<>(index, serializer, deserializer, null, changed, defaultValue);
    }

    public static <T> TwoWaySyncProperty<T> forServer(int index, Function<T, Integer> serializer, Function<Integer, T> deserializer, Supplier<T> supplier, Consumer<T> changed) {
        return new TwoWaySyncProperty<>(index, serializer, deserializer, supplier, changed, null);
    }

    private TwoWaySyncProperty(int index, Function<T, Integer> serializer, Function<Integer, T> deserializer, Supplier<T> supplier, Consumer<T> changed, T defaultValue) {
        this.index = index;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.supplier = supplier;
        this.changed = changed;
        this.value = defaultValue;
    }

    public void syncToServer(T newValue) {
        PacketUtil.sendToServer(PropertyChangePacket.ID, buf -> {
            buf.writeInt(index);
            buf.writeInt(serializer.apply(newValue));
        });
    }

    public T getDeserialized() {
        return value;
    }

    @Override
    public int get() {
        return supplier != null ? serializer.apply(supplier.get()) : serializer.apply(value);
    }

    @Override
    public void set(int value) {
        T deserializedValue = deserializer.apply(value);
        changed.accept(deserializedValue);
        this.value = deserializedValue;
    }
}
