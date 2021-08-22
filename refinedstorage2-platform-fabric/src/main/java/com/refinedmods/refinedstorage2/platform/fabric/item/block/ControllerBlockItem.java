package com.refinedmods.refinedstorage2.platform.fabric.item.block;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ControllerBlockItem extends ColoredBlockItem {
    private static final int MAX_DAMAGE = 100;

    public ControllerBlockItem(Block block, Settings settings, DyeColor color, Text displayName) {
        super(block, settings.maxDamage(MAX_DAMAGE), color, displayName);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        long cap = getCapacity(stack);
        if (cap > 0) {
            tooltip.add(Rs2Mod.createTranslation(
                    "misc",
                    "stored_with_capacity",
                    QuantityFormatter.format(getStored(stack)),
                    QuantityFormatter.format(cap)
            ).formatted(Formatting.GRAY));
        }
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        boolean result = super.postPlacement(pos, world, player, stack, state);
        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.receive(getStored(stack), Action.EXECUTE);
            }
        }
        return result;
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
        NbtCompound tag = stack.getNbt();
        if (tag == null) {
            return 0;
        }
        return tag.getLong("stored");
    }

    private static long getCapacity(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null) {
            return 0;
        }
        return tag.getLong("cap");
    }

    public static void setEnergy(ItemStack stack, long stored, long capacity) {
        if (!stack.hasNbt()) {
            stack.setNbt(new NbtCompound());
        }
        stack.getNbt().putLong("stored", stored);
        stack.getNbt().putLong("cap", capacity);
    }

    public static int calculateDamage(long stored, long capacity) {
        return MAX_DAMAGE - (int) (((double) stored / (double) capacity) * MAX_DAMAGE);
    }
}
