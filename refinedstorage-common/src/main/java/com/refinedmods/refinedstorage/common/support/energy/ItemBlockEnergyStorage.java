package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractListeningEnergyStorage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ItemBlockEnergyStorage extends AbstractListeningEnergyStorage {
    private static final String TAG_STORED = "stored";

    private final ItemStack stack;
    private final BlockEntityType<?> blockEntityType;

    public ItemBlockEnergyStorage(final EnergyStorage energyStorage,
                                  final ItemStack stack,
                                  final BlockEntityType<?> blockEntityType) {
        super(energyStorage);
        this.stack = stack;
        this.blockEntityType = blockEntityType;
        final CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            readFromTag(energyStorage, customData.copyTag());
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected void onStoredChanged(final long stored) {
        final CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        final CompoundTag tag = customData == null ? new CompoundTag() : customData.copyTag();
        writeToTag(tag, stored);
        BlockItem.setBlockEntityData(stack, blockEntityType, tag);
    }

    public static void writeToTag(final CompoundTag tag, final long stored) {
        tag.putLong(TAG_STORED, stored);
    }

    public static void readFromTag(final EnergyStorage energyStorage, final CompoundTag tag) {
        final long stored = tag.getLong(TAG_STORED);
        energyStorage.receive(stored, Action.EXECUTE);
    }
}
