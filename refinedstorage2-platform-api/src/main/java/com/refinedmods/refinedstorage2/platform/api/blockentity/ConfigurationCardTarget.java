package com.refinedmods.refinedstorage2.platform.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ConfigurationCardTarget {
    BlockEntityType<?> getBlockEntityType();

    void writeConfiguration(CompoundTag tag);

    void readConfiguration(CompoundTag tag);
}
