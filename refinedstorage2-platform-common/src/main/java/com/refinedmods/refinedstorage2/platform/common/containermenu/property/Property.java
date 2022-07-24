package com.refinedmods.refinedstorage2.platform.common.containermenu.property;

import net.minecraft.world.inventory.DataSlot;

public interface Property<T> {
    PropertyType<T> getType();

    T getValue();

    DataSlot getDataSlot();
}
