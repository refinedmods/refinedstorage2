package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientFluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluidGridContainerMenu extends AbstractGridContainerMenu<FluidResource> implements FluidGridEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidGridContainerMenu.class);

    private final FluidGridEventHandler fluidGridEventHandler;

    public FluidGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, buf, createViewBuilder());
        this.fluidGridEventHandler = new ClientFluidGridEventHandler();
    }

    public FluidGridContainerMenu(final int syncId,
                                  final Inventory playerInventory,
                                  final FluidGridBlockEntity grid,
                                  final ExtractableStorage<ItemResource> bucketStorage) {
        super(Menus.INSTANCE.getFluidGrid(), syncId, playerInventory, grid, createViewBuilder());
        final GridService<FluidResource> gridService = grid.getNode().createService(
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

    private static GridViewBuilder<FluidResource> createViewBuilder() {
        return new GridViewBuilderImpl<>(Platform.INSTANCE.getFluidGridResourceFactory());
    }

    @Override
    protected ResourceAmount<FluidResource> readResourceAmount(final FriendlyByteBuf buf) {
        return PacketUtil.readFluidResourceAmount(buf);
    }

    @Override
    public void onChanged(final ResourceListOperationResult<FluidResource> change,
                          @Nullable final TrackedResource trackedResource) {
        final FluidResource resource = change.resourceAmount().getResource();

        LOGGER.debug("Received a change of {} for {}", change.change(), resource);

        Platform.INSTANCE.getServerToClientCommunications().sendGridFluidUpdate(
            (ServerPlayer) playerInventory.player,
            resource,
            change.change(),
            trackedResource
        );
    }

    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                fluidGridEventHandler.onTransfer(slot.getContainerSlot());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        fluidGridEventHandler.onInsert(insertMode);
    }

    @Override
    public void onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        fluidGridEventHandler.onExtract(fluidResource, mode, cursor);
    }
}
