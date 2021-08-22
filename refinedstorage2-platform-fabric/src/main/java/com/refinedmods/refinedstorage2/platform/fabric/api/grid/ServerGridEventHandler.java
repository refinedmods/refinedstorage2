package com.refinedmods.refinedstorage2.platform.fabric.api.grid;

import com.refinedmods.refinedstorage2.api.grid.GridEventHandlerImpl;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import net.minecraft.server.network.ServerPlayerEntity;

public class ServerGridEventHandler extends GridEventHandlerImpl {
    private final ServerPlayerEntity player;

    public ServerGridEventHandler(boolean active, StorageChannel<Rs2ItemStack> storageChannel, ServerPlayerEntity player) {
        super(active, storageChannel, new PlayerGridInteractor(player));
        this.player = player;
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        ServerPacketUtil.sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }
}
