package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public abstract class AbstractEnergyItem extends Item implements EnergyItem {
    protected AbstractEnergyItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
        final ItemStack stack,
        @Nullable final Level level,
        final List<Component> lines,
        final TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, lines, flag);
        PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> {
            final long stored = energyStorage.getStored();
            final long capacity = energyStorage.getCapacity();
            final double pct = stored / (double) capacity;
            lines.add(PlatformApi.INSTANCE.createStoredWithCapacityTranslation(stored, capacity, pct)
                .withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return PlatformApi.INSTANCE.getEnergyStorage(stack).isPresent();
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return PlatformApi.INSTANCE.getEnergyStorage(stack).map(energyStorage -> (int) Math.round(
            (energyStorage.getStored() / (double) energyStorage.getCapacity()) * 13D
        )).orElse(0);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return PlatformApi.INSTANCE.getEnergyStorage(stack).map(energyStorage -> Mth.hsvToRgb(
            Math.max(0.0F, (float) energyStorage.getStored() / (float) energyStorage.getCapacity()) / 3.0F,
            1.0F,
            1.0F
        )).orElse(0);
    }

    public ItemStack createAtEnergyCapacity() {
        final ItemStack stack = new ItemStack(this);
        PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> energyStorage.receive(
            energyStorage.getCapacity(),
            Action.EXECUTE
        ));
        return stack;
    }
}
