package com.refinedmods.refinedstorage2.platform.common.internal.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.blockentity.EnergyBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage2.platform.api.item.EnergyItemHelper;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createStoredWithCapacityTranslation;

public class EnergyItemHelperImpl implements EnergyItemHelper {
    @Override
    public void addTooltip(final ItemStack stack, final List<Component> lines) {
        PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> {
            final long stored = energyStorage.getStored();
            final long capacity = energyStorage.getCapacity();
            final double pct = stored / (double) capacity;
            lines.add(createStoredWithCapacityTranslation(stored, capacity, pct).withStyle(ChatFormatting.GRAY));
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

    @Override
    public ItemStack createAtEnergyCapacity(final Item item) {
        final ItemStack stack = item.getDefaultInstance();
        PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(energyStorage -> energyStorage.receive(
            energyStorage.getCapacity(),
            Action.EXECUTE
        ));
        return stack;
    }

    @Override
    public void passEnergyToBlockEntity(final BlockPos pos, final Level level, final ItemStack stack) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof EnergyBlockEntity energyBlockEntity)) {
            return;
        }
        PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(
            energyStorage -> energyBlockEntity.getEnergyStorage().receive(energyStorage.getStored(), Action.EXECUTE)
        );
    }

    public static Stream<ItemStack> createAllAtEnergyCapacity(final List<Supplier<BlockItem>> items) {
        return items.stream().map(Supplier::get)
            .filter(AbstractEnergyBlockItem.class::isInstance)
            .map(AbstractEnergyBlockItem.class::cast)
            .map(AbstractEnergyBlockItem::createAtEnergyCapacity);
    }
}
