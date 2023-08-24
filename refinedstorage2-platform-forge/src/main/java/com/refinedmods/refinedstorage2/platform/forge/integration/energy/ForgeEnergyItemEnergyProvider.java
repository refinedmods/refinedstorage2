package com.refinedmods.refinedstorage2.platform.forge.integration.energy;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.item.ItemEnergyProvider;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public class ForgeEnergyItemEnergyProvider implements ItemEnergyProvider {
    @Override
    public boolean isEnabled() {
        return Platform.INSTANCE.getConfig().getWirelessGrid().getUseEnergy();
    }

    @Override
    public long getStored(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public long getCapacity(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    @Override
    public void drain(final Player player, final PlayerSlotReference slotReference, final long amount) {
        if (!isEnabled()) {
            return;
        }
        final ItemStack stack = player.getInventory().getItem(slotReference.getSlotIndex());
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(
            energyStorage -> energyStorage.extractEnergy((int) amount, false)
        );
    }

    @Override
    public ItemStack getAtCapacity(final ItemStack stack) {
        final ItemStack copied = stack.copy();
        copied.getCapability(ForgeCapabilities.ENERGY).ifPresent(
            energyStorage -> energyStorage.receiveEnergy(energyStorage.getMaxEnergyStored(), false)
        );
        return copied;
    }
}
