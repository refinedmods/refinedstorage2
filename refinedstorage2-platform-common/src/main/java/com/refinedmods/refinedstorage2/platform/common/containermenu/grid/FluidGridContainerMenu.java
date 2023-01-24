package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientFluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class FluidGridContainerMenu extends AbstractGridContainerMenu<FluidResource> implements FluidGridEventHandler {
    private final FluidGridEventHandler fluidGridEventHandler;

    public FluidGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, buf);
        this.fluidGridEventHandler = new ClientFluidGridEventHandler();
    }

    public FluidGridContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final FluidGridBlockEntity grid,
                                  final ExtractableStorage<ItemResource> bucketStorage) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, grid);
        final GridService<FluidResource> gridService = grid.getNode().create(
            StorageChannelTypes.FLUID,
            new PlayerActor(playerInventory.player),
            resource -> Long.MAX_VALUE,
            Platform.INSTANCE.getBucketAmount()
        );
        this.fluidGridEventHandler = Platform.INSTANCE.createFluidGridEventHandler(
            this,
            gridService,
            playerInventory,
            bucketStorage
        );
    }

    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        fluidGridEventHandler.onExtract(fluidResource, mode, cursor);
    }
}
