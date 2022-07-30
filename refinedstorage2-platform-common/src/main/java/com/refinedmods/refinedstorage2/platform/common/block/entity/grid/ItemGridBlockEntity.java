package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class ItemGridBlockEntity extends AbstractGridBlockEntity<ItemResource> {
    public ItemGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getGrid(), pos, state, StorageChannelTypes.ITEM);
    }

    @Override
    protected void writeResourceAmount(final FriendlyByteBuf buf, final ResourceAmount<ItemResource> stack) {
        PacketUtil.writeItemResourceAmount(buf, stack);
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new ItemGridContainerMenu(syncId, inv, this);
    }
}
