package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockItem extends CreativeControllerBlockItem {
    public ControllerBlockItem(Block block, CreativeModeTab tab, Component displayName) {
        super(block, tab, displayName);
    }

    public static float getPercentFull(ItemStack stack) {
        CompoundTag tag = getBlockEntityData(stack);
        if (tag == null) {
            return 1;
        }
        long stored = ControllerBlockEntity.getStored(tag);
        long capacity = ControllerBlockEntity.getCapacity(tag);
        if (capacity == 0) {
            return 1;
        }
        return (float) stored / (float) capacity;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ControllerBlockEntity.hasEnergy(getBlockEntityData(stack));
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getPercentFull(stack) * 13F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, getPercentFull(stack)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);

        CompoundTag data = getBlockEntityData(stack);
        if (ControllerBlockEntity.hasEnergy(data)) {
            long stored = ControllerBlockEntity.getStored(data);
            long capacity = ControllerBlockEntity.getCapacity(data);
            tooltip.add(createTranslation("misc", "stored_with_capacity", QuantityFormatter.format(stored), QuantityFormatter.format(capacity)).withStyle(ChatFormatting.GRAY));
        }
    }
}
