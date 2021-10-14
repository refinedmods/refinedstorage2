package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.ClientFluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.FluidGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidGridScreenHandler extends GridScreenHandler<FluidResource> implements FluidGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final GridService<FluidResource> gridService;
    private final FluidGridEventHandler fluidGridEventHandler;

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, buf, createView());
        this.gridService = null;
        this.fluidGridEventHandler = new ClientFluidGridEventHandler();
    }

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, FluidGridBlockEntity grid, ExtractableStorage<ItemResource> bucketStorage) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, grid, createView());
        this.gridService = new GridServiceImpl<>(storageChannel, new PlayerSource(playerInventory.player), (resource) -> FluidConstants.BUCKET);
        this.grid.addWatcher(this);
        this.fluidGridEventHandler = new FluidGridEventHandlerImpl(this, gridService, playerInventory, bucketStorage);
    }

    private static GridViewImpl<FluidResource> createView() {
        return new GridViewImpl<>(new FluidGridResourceFactory(), new ResourceListImpl<>());
    }

    @Override
    protected ResourceAmount<FluidResource> readStack(PacketByteBuf buf) {
        return PacketUtil.readFluidResourceAmount(buf);
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void onChanged(ResourceListOperationResult<FluidResource> change) {
        LOGGER.info("Received a change of {} for {}", change.change(), change.resourceAmount().getResource());

        ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerInventory.player, PacketIds.GRID_FLUID_UPDATE, buf -> {
            PacketUtil.writeFluidResource(buf, change.resourceAmount().getResource());
            buf.writeLong(change.change());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.resourceAmount().getResource());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {

                /* TODO Rs2ItemStack slotStack = Rs2PlatformApiFacade.INSTANCE.itemStackConversion().toDomain(slot.getStack());
                ItemStack resultingStack = Rs2PlatformApiFacade.INSTANCE.itemStackConversion().toPlatform(eventHandler.onInsertFromTransfer(slotStack));
                slot.setStack(resultingStack);*/
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        fluidGridEventHandler.onInsert(insertMode);
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        fluidGridEventHandler.onExtract(fluidResource, mode, cursor);
    }
}
