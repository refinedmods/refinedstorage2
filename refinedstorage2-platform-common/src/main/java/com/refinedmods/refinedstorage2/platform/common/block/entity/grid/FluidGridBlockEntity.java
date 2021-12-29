package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FluidGridBlockEntity extends GridBlockEntity<FluidResource> {
    public FluidGridBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.INSTANCE.getFluidGrid(), pos, state, StorageChannelTypes.FLUID);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "fluid_grid");
    }

    @Override
    protected void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<FluidResource> resourceAmount) {
        PacketUtil.writeFluidResourceAmount(buf, resourceAmount);
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FluidGridContainerMenu(syncId, inv, this, getBucketStorage());
    }

    private ExtractableStorage<ItemResource> getBucketStorage() {
        return getNode()
                .getNetwork()
                .getComponent(StorageNetworkComponent.class)
                .getStorageChannel(StorageChannelTypes.ITEM);
    }
}
