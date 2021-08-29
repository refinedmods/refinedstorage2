package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FabricFluidGridStackFactory;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidGridScreenHandler extends GridScreenHandler<Rs2FluidStack> implements GridWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, buf, createView());
    }

    public FluidGridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity<Rs2FluidStack> grid) {
        super(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), syncId, playerInventory, grid, createView());
        this.grid.addWatcher(this);
    }

    private static GridViewImpl<Rs2FluidStack, Rs2FluidStackIdentifier> createView() {
        return new GridViewImpl<>(new FabricFluidGridStackFactory(), Rs2FluidStackIdentifier::new, StackListImpl.createFluidStackList());
    }

    @Override
    public void onActiveChanged(boolean active) {
        setActive(active);
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
}
