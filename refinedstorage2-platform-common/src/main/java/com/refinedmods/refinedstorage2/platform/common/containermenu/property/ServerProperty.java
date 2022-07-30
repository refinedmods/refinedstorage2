package com.refinedmods.refinedstorage2.platform.common.containermenu.property;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.inventory.DataSlot;

public class ServerProperty<T> extends DataSlot implements Property<T> {
    private final PropertyType<T> type;
    private final Supplier<T> supplier;
    private final Consumer<T> consumer;

    public ServerProperty(final PropertyType<T> type, final Supplier<T> supplier, final Consumer<T> consumer) {
        this.type = type;
        this.supplier = supplier;
        this.consumer = consumer;
    }

    @Override
    public PropertyType<T> getType() {
        return type;
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataSlot getDataSlot() {
        return this;
    }

    @Override
    public int get() {
        return type.serializer().apply(supplier.get());
    }

    @Override
    public void set(final int newValue) {
        consumer.accept(type.deserializer().apply(newValue));
    }
}
