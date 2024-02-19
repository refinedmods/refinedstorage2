package com.refinedmods.refinedstorage2.platform.common.support.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.AbstractProxyEnergyStorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ItemBlockEnergyStorage extends AbstractProxyEnergyStorage {
    private static final String TAG_STORED = "stored";

    private final ItemStack stack;
    private final BlockEntityType<?> blockEntityType;

    public ItemBlockEnergyStorage(final EnergyStorage energyStorage,
                                  final ItemStack stack,
                                  final BlockEntityType<?> blockEntityType) {
        super(energyStorage);
        this.stack = stack;
        this.blockEntityType = blockEntityType;
        final CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) {
            readFromTag(energyStorage, tag);
        }
    }

    @Override
    public long receive(final long amount, final Action action) {
        final long received = super.receive(amount, action);
        if (received > 0 && action == Action.EXECUTE) {
            updateStored();
        }
        return received;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long extracted = super.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            updateStored();
        }
        return extracted;
    }

    private void updateStored() {
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag == null) {
            tag = new CompoundTag();
        }
        writeToTag(tag, getStored());
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
