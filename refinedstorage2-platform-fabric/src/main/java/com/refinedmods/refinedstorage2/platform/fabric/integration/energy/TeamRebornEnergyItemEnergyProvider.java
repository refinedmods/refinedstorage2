package com.refinedmods.refinedstorage2.platform.fabric.integration.energy;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.item.ItemEnergyProvider;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class TeamRebornEnergyItemEnergyProvider implements ItemEnergyProvider {
    @Override
    public boolean isEnabled() {
        return Platform.INSTANCE.getConfig().getWirelessGrid().getUseEnergy();
    }

    @Override
    public long getStored(final ItemStack stack) {
        final EnergyStorage energyStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (energyStorage == null) {
            return 0;
        }
        return energyStorage.getAmount();
    }

    @Override
    public long getCapacity(final ItemStack stack) {
        final EnergyStorage energyStorage = EnergyStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (energyStorage == null) {
            return 0;
        }
        return energyStorage.getCapacity();
    }

    @Override
    public void drain(final Player player, final PlayerSlotReference slotReference, final long amount) {
        if (!isEnabled()) {
            return;
        }
        final PlayerInventoryStorage inventoryStorage = PlayerInventoryStorage.of(player);
        final ContainerItemContext ctx = ContainerItemContext.ofPlayerSlot(
            player,
            inventoryStorage.getSlot(slotReference.getSlotIndex())
        );
        final EnergyStorage energyStorage = EnergyStorage.ITEM.find(ctx.getItemVariant().toStack(), ctx);
        if (energyStorage == null) {
            return;
        }
        try (Transaction tx = Transaction.openOuter()) {
            energyStorage.extract(amount, tx);
            tx.commit();
        }
    }

    @Override
    public ItemStack getAtCapacity(final ItemStack stack) {
        final ItemStack copied = stack.copy();
        SimpleEnergyItem.setStoredEnergyUnchecked(copied, getCapacity(copied));
        return copied;
    }
}
