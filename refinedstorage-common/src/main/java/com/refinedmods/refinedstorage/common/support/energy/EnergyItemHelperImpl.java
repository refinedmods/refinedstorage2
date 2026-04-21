package com.refinedmods.refinedstorage.common.support.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createStoredWithCapacityTranslation;

public class EnergyItemHelperImpl implements EnergyItemHelper {
    @Override
    public void addTooltip(final ItemStack stack, final Consumer<Component> builder) {
        if (!RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            return;
        }
        RefinedStorageApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> {
            final long stored = energyStorage.getStored();
            final long capacity = energyStorage.getCapacity();
            final double pct = stored / (double) capacity;
            builder.accept(createStoredWithCapacityTranslation(stored, capacity, pct).withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        if (!RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            return false;
        }
        return RefinedStorageApi.INSTANCE.getEnergyStorage(stack).isPresent();
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return RefinedStorageApi.INSTANCE.getEnergyStorage(stack).map(energyStorage -> (int) Math.round(
            (energyStorage.getStored() / (double) energyStorage.getCapacity()) * 13D
        )).orElse(0);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return RefinedStorageApi.INSTANCE.getEnergyStorage(stack).map(energyStorage -> Mth.hsvToRgb(
            Math.max(0.0F, (float) energyStorage.getStored() / (float) energyStorage.getCapacity()) / 3.0F,
            1.0F,
            1.0F
        )).orElse(0);
    }

    @Override
    public ItemStack createAtEnergyCapacity(final Item item) {
        final ItemStack stack = item.getDefaultInstance();
        RefinedStorageApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> energyStorage.receive(
            energyStorage.getCapacity(),
            Action.EXECUTE
        ));
        return stack;
    }
}
