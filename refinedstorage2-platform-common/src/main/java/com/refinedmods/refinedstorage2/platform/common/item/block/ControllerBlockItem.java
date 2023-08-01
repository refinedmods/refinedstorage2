package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockItem extends CreativeControllerBlockItem {
    public ControllerBlockItem(final Block block, final Component displayName) {
        super(block, displayName);
    }

    public static float getPercentFull(final ItemStack stack) {
        final CompoundTag tag = getBlockEntityData(stack);
        if (tag == null) {
            return 1;
        }
        final long stored = ControllerBlockEntity.getStored(tag);
        final long capacity = ControllerBlockEntity.getCapacity(tag);
        if (capacity == 0) {
            return 1;
        }
        return (float) stored / (float) capacity;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return ControllerBlockEntity.hasEnergy(getBlockEntityData(stack));
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return Math.round(getPercentFull(stack) * 13F);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, getPercentFull(stack)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        final CompoundTag data = getBlockEntityData(stack);
        if (ControllerBlockEntity.hasEnergy(data)) {
            final long stored = ControllerBlockEntity.getStored(data);
            final long capacity = ControllerBlockEntity.getCapacity(data);
            tooltip.add(createTranslation(
                "misc",
                "stored_with_capacity",
                AmountFormatting.format(stored),
                AmountFormatting.format(capacity)
            ).withStyle(ChatFormatting.GRAY));
        }
    }
}
