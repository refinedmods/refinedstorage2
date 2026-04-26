package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.TransferableBlockEntityEnergy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class AbstractEnergyLootItemFunction implements LootItemFunction {
    @Override
    public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TransferableBlockEntityEnergy transferableBlockEntityEnergy) {
            final long stored = transferableBlockEntityEnergy.getEnergyStorage().getStored();
            final SimpleEnergyItemContext context = new SimpleEnergyItemContext(stack);
            RefinedStorageApi.INSTANCE.getEnergyStorage(stack, context).ifPresent(
                energyStorage -> energyStorage.receive(stored, Action.EXECUTE)
            );
            return context.copyStack();
        }
        return stack;
    }
}
