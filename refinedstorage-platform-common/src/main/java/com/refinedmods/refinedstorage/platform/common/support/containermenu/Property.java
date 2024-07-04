package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import net.minecraft.world.inventory.DataSlot;

public interface Property<T> {
    PropertyType<T> getType();

    T getValue();

    DataSlot getDataSlot();
}
