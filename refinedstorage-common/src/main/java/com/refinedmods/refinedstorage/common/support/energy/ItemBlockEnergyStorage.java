package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractListeningEnergyStorage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        final TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            blockEntityData.copyTagWithoutId().getLong(TAG_STORED)
                .ifPresent(stored -> energyStorage.receive(stored, Action.EXECUTE));
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected void onStoredChanged(final long stored) {
        final TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        final CompoundTag tag = blockEntityData == null ? new CompoundTag() : blockEntityData.copyTagWithoutId();
        tag.putLong(TAG_STORED, stored);
        stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntityType, tag));
    }

    public static void store(final ValueOutput output, final long stored) {
        output.putLong(TAG_STORED, stored);
    }

    public static void read(final EnergyStorage energyStorage, final ValueInput input) {
        input.getLong(TAG_STORED).ifPresent(stored -> energyStorage.receive(stored, Action.EXECUTE));
    }
}
