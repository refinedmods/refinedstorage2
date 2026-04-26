package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractListeningEnergyStorage;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ItemBlockEnergyStorage extends AbstractListeningEnergyStorage {
    private static final String TAG_STORED = "stored";

    private final BlockEntityType<?> blockEntityType;
    private final EnergyItemContext context;

    public ItemBlockEnergyStorage(final EnergyStorage delegate,
                                  final ItemStack stack,
                                  final BlockEntityType<?> blockEntityType,
                                  final EnergyItemContext context) {
        super(delegate);
        this.blockEntityType = blockEntityType;
        this.context = context;
        updateStored(stack, delegate);
    }

    private static void updateStored(final ItemStack stack, final EnergyStorage delegate) {
        final TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            blockEntityData.copyTagWithoutId().getLong(TAG_STORED)
                .ifPresent(stored -> delegate.receive(stored, Action.EXECUTE));
        }
    }

    @Override
    protected void onStoredChanged(final long stored) {
        final ItemStack copiedStack = context.copyStack();
        final TypedEntityData<BlockEntityType<?>> blockEntityData = copiedStack.get(DataComponents.BLOCK_ENTITY_DATA);
        final CompoundTag tag = blockEntityData == null ? new CompoundTag() : blockEntityData.copyTagWithoutId();
        tag.putLong(TAG_STORED, stored);
        copiedStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntityType, tag));
        context.setStack(copiedStack);
    }

    public static void store(final ValueOutput output, final long stored) {
        output.putLong(TAG_STORED, stored);
    }

    public static void read(final EnergyStorage energyStorage, final ValueInput input) {
        input.getLong(TAG_STORED).ifPresent(stored -> energyStorage.receive(stored, Action.EXECUTE));
    }
}
