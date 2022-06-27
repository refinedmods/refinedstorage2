package com.refinedmods.refinedstorage2.platform.api.item.block;

import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class StorageBlockBlockItem extends BlockItem {
    protected StorageBlockBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        return StorageItemHelper.tryDisassembly(level, player, stack, createPrimaryDisassemblyByproduct(stack.getCount()), createSecondaryDisassemblyByproduct(stack.getCount()));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(final BlockPos pos, final Level level, @Nullable final Player player, final ItemStack stack, final BlockState state) {
        if (!level.isClientSide()) {
            updateBlockEntityTag(pos, level, stack);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void updateBlockEntityTag(final BlockPos pos, final Level level, final ItemStack stack) {
        StorageItemHelper.getStorageId(stack).ifPresent(id -> updateBlockEntityWithStorageId(pos, level.getBlockEntity(pos), id));
    }

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);

    protected abstract void updateBlockEntityWithStorageId(BlockPos pos, @Nullable BlockEntity blockEntity, UUID id);
}
