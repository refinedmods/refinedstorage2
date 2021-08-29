package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.ItemGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ItemGridBlockEntity extends GridBlockEntity<Rs2ItemStack> {
    public ItemGridBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getGrid(), pos, state, StorageChannelTypes.ITEM);
    }

    @Override
    protected void writeStack(PacketByteBuf buf, Rs2ItemStack stack) {
        PacketUtil.writeItemStack(buf, stack, true);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ItemGridScreenHandler(syncId, inv, this);
    }
}
