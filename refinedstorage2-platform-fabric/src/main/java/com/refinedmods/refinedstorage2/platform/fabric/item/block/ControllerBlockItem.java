package com.refinedmods.refinedstorage2.platform.fabric.item.block;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ControllerBlockItem extends ColoredBlockItem {
    private static final String TAG_STORED = "stored";
    private static final String TAG_CAPACITY = "cap";

    public ControllerBlockItem(Block block, Properties properties, DyeColor color, Component displayName) {
        super(block, properties, color, displayName);
    }

    public static float getPercentFull(ItemStack stack) {
        long stored = getStored(stack);
        long capacity = getCapacity(stack);
        if (capacity == 0) {
            return 1;
        }
        return (float) stored / (float) capacity;
    }

    private static long getStored(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getLong(TAG_STORED);
    }

    private static long getCapacity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getLong(TAG_CAPACITY);
    }

    public static void setEnergy(ItemStack stack, long stored, long capacity) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putLong(TAG_STORED, stored);
        stack.getTag().putLong(TAG_CAPACITY, capacity);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_STORED) && stack.getTag().contains(TAG_CAPACITY);
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);

        long cap = getCapacity(stack);
        if (cap > 0) {
            tooltip.add(Rs2Mod.createTranslation("misc", "stored_with_capacity", QuantityFormatter.format(getStored(stack)), QuantityFormatter.format(cap)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.getContainer().getNode().receive(getStored(stack), Action.EXECUTE);
            }
        }
        return result;
    }
}
