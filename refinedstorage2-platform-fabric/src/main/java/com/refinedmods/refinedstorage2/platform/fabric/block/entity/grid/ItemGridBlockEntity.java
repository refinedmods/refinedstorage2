package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemGridBlockEntity extends GridBlockEntity<ItemResource> {
    public ItemGridBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getGrid(), pos, state, StorageChannelTypes.ITEM);
    }

    @Override
    protected void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<ItemResource> stack) {
        PacketUtil.writeItemResourceAmount(buf, stack);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new ItemGridContainerMenu(syncId, inv, this);
    }
}
