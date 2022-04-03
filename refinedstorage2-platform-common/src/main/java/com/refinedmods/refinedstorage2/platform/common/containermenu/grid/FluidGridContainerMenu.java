package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientFluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidGridContainerMenu extends GridContainerMenu<FluidResource> implements FluidGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final GridService<FluidResource> gridService;
    private final FluidGridEventHandler fluidGridEventHandler;

    public FluidGridContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, buf, createView());
        this.gridService = null;
        this.fluidGridEventHandler = new ClientFluidGridEventHandler();
    }

    public FluidGridContainerMenu(int syncId, Inventory playerInventory, FluidGridBlockEntity grid, ExtractableStorage<ItemResource> bucketStorage) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, grid, createView());
        this.gridService = new GridServiceImpl<>(storageChannel, new PlayerSource(playerInventory.player), resource -> Long.MAX_VALUE, Platform.INSTANCE.getBucketAmount());
        this.grid.addWatcher(this);
        this.fluidGridEventHandler = Platform.INSTANCE.createFluidGridEventHandler(this, gridService, playerInventory, bucketStorage);
    }

    private static GridViewImpl<FluidResource> createView() {
        return new GridViewImpl<>(Platform.INSTANCE.getFluidGridResourceFactory(), new ResourceListImpl<>());
    }

    @Override
    protected ResourceAmount<FluidResource> readResourceAmount(FriendlyByteBuf buf) {
        return PacketUtil.readFluidResourceAmount(buf);
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void onChanged(ResourceListOperationResult<FluidResource> change) {
        FluidResource resource = change.resourceAmount().getResource();

        LOGGER.info("Received a change of {} for {}", change.change(), resource);

        Platform.INSTANCE.getServerToClientCommunications().sendGridFluidUpdate(
                (ServerPlayer) playerInventory.player,
                resource,
                change.change(),
                storageChannel.findTrackedResourceBySourceType(resource, PlayerSource.class).orElse(null)
        );
    }

    @Override
    public ItemStack quickMoveStack(Player playerEntity, int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                fluidGridEventHandler.onTransfer(slot.getContainerSlot());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        fluidGridEventHandler.onInsert(insertMode);
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        fluidGridEventHandler.onExtract(fluidResource, mode, cursor);
    }
}
