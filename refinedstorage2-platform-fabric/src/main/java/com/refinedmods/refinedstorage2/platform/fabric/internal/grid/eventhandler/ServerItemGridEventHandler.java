package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.ItemGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import net.minecraft.server.network.ServerPlayerEntity;

public class ServerItemGridEventHandler extends ItemGridEventHandlerImpl {
    private final ServerPlayerEntity player;

    public ServerItemGridEventHandler(boolean active, StorageChannel<Rs2ItemStack> storageChannel, ServerPlayerEntity player) {
        super(active, storageChannel, new PlayerGridInteractor(player));
        this.player = player;
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        ServerPacketUtil.sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }
}
