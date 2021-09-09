package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler.ClientFluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler.PlayerFluidGridInteractor;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FabricFluidGridStackFactory;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.item.InventoryStorageImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class FluidGridScreenHandler extends GridScreenHandler<Rs2FluidStack> implements FluidGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FluidGridEventHandler eventHandler;

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, buf, createView());
        this.eventHandler = new ClientFluidGridEventHandler();
    }

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity<Rs2FluidStack> grid) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, grid, createView());
        this.grid.addWatcher(this);
        this.eventHandler = new FluidGridEventHandlerImpl(new PlayerFluidGridInteractor(playerInventory.player), storageChannel, grid.getContainer().getNode().isActive());
    }

    private static GridViewImpl<Rs2FluidStack, Rs2FluidStackIdentifier> createView() {
        return new GridViewImpl<>(new FabricFluidGridStackFactory(), Rs2FluidStackIdentifier::new, StackListImpl.createFluidStackList());
    }

    @Override
    public void onInsertFromCursor() {
        eventHandler.onInsertFromCursor();
    }

    @Override
    public long onInsertFromTransfer(Rs2FluidStack stack) {
        return eventHandler.onInsertFromTransfer(stack);
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        eventHandler.onActiveChanged(active);
    }

    @Override
    protected Rs2FluidStack readStack(PacketByteBuf buf) {
        return PacketUtil.readFluidStack(buf, true);
    }

    @Override
    public void onChanged(StackListResult<Rs2FluidStack> change) {
        LOGGER.info("Received a change of {} for {}", change.change(), change.stack());

        ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerInventory.player, PacketIds.GRID_FLUID_UPDATE, buf -> {
            PacketUtil.writeFluidStack(buf, change.stack(), false);
            buf.writeLong(change.change());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.stack());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                Storage<FluidVariant> storage = findFluidStorageFromSlot(playerEntity, (SlotAccessor) slot);
                if (storage == null) {
                    return ItemStack.EMPTY;
                }

                try (Transaction trans = Transaction.openOuter()) {
                    ResourceAmount<FluidVariant> extracted = extractAllFromStorage(storage, trans);
                    Rs2FluidStack extractedDomain = Rs2PlatformApiFacade.INSTANCE.fluidResourceAmountConversion().toDomain(extracted);
                    long remainder = eventHandler.onInsertFromTransfer(extractedDomain);
                    storage.insert(extracted.resource(), remainder, trans);
                    trans.commit();
                }

                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    private ResourceAmount<FluidVariant> extractAllFromStorage(Storage<FluidVariant> storage, Transaction trans) {
        FluidVariant resource = StorageUtil.findExtractableResource(storage, trans);
        long extracted = storage.extract(resource, Long.MAX_VALUE, trans);
        return new ResourceAmount<>(
                resource,
                extracted
        );
    }

    @Nullable
    private Storage<FluidVariant> findFluidStorageFromSlot(PlayerEntity playerEntity, SlotAccessor slot) {
        return ContainerItemContext.ofPlayerSlot(
                playerEntity,
                InventoryStorageImpl.of(playerInventory, null).getSlot(slot.getIndex())
        ).find(FluidStorage.ITEM);
    }
}
