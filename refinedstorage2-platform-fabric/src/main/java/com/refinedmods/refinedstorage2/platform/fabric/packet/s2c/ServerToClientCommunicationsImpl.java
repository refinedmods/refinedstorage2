package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketUtil;

import java.util.UUID;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    @Override
    public void sendControllerEnergy(ServerPlayer player, long stored, long capacity) {
        sendToPlayer(player, PacketIds.CONTROLLER_ENERGY, buf -> {
            buf.writeLong(stored);
            buf.writeLong(capacity);
        });
    }

    @Override
    public void sendGridActiveness(ServerPlayer player, boolean active) {
        sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }

    @Override
    public void sendGridFluidUpdate(ServerPlayer player, FluidResource fluidResource, long change, StorageTracker.Entry trackerEntry) {
        sendToPlayer(player, PacketIds.GRID_FLUID_UPDATE, buf -> {
            PacketUtil.writeFluidResource(buf, fluidResource);
            buf.writeLong(change);
            PacketUtil.writeTrackerEntry(buf, trackerEntry);
        });
    }

    @Override
    public void sendGridItemUpdate(ServerPlayer player, ItemResource itemResource, long change, StorageTracker.Entry trackerEntry) {
        sendToPlayer(player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemResource(buf, itemResource);
            buf.writeLong(change);
            PacketUtil.writeTrackerEntry(buf, trackerEntry);
        });
    }

    @Override
    public void sendResourceFilterSlotUpdate(ServerPlayer player, ResourceFilterContainer resourceFilterContainer, int slotIndex) {
        sendToPlayer(player, PacketIds.RESOURCE_FILTER_SLOT_UPDATE, buf -> {
            buf.writeInt(slotIndex);
            resourceFilterContainer.writeToUpdatePacket(slotIndex, buf);
        });
    }

    @Override
    public void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo) {
        sendToPlayer(player, PacketIds.STORAGE_INFO_RESPONSE, bufToSend -> {
            bufToSend.writeUUID(id);
            bufToSend.writeLong(storageInfo.stored());
            bufToSend.writeLong(storageInfo.capacity());
        });
    }

    private static void sendToPlayer(ServerPlayer playerEntity, ResourceLocation id, Consumer<FriendlyByteBuf> bufConsumer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}
