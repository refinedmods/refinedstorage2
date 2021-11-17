package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidGridBlockEntity extends GridBlockEntity<FluidResource> {
    public FluidGridBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getFluidGrid(), pos, state, StorageChannelTypes.FLUID);
    }

    @Override
    public Component getDisplayName() {
        return Rs2Mod.createTranslation("block", "fluid_grid");
    }

    @Override
    protected void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<FluidResource> resourceAmount) {
        PacketUtil.writeFluidResourceAmount(buf, resourceAmount);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FluidGridScreenHandler(syncId, inv, this, getBucketStorage());
    }

    private ExtractableStorage<ItemResource> getBucketStorage() {
        return getContainer()
                .getNode()
                .getNetwork()
                .getComponent(StorageNetworkComponent.class)
                .getStorageChannel(StorageChannelTypes.ITEM);
    }
}
