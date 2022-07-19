package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    @Override
    public void sendControllerEnergy(final ServerPlayer player, final long stored, final long capacity) {
        sendToPlayer(player, PacketIds.CONTROLLER_ENERGY, buf -> {
            buf.writeLong(stored);
            buf.writeLong(capacity);
        });
    }

    @Override
    public void sendGridActiveness(final ServerPlayer player, final boolean active) {
        sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }

    @Override
    public void sendGridFluidUpdate(final ServerPlayer player,
                                    final FluidResource fluidResource,
                                    final long change,
                                    @Nullable final TrackedResource trackerEntry) {
        sendToPlayer(player, PacketIds.GRID_FLUID_UPDATE, buf -> {
            PacketUtil.writeFluidResource(buf, fluidResource);
            buf.writeLong(change);
            PacketUtil.writeTrackedResource(buf, trackerEntry);
        });
    }

    @Override
    public void sendGridItemUpdate(final ServerPlayer player,
                                   final ItemResource itemResource,
                                   final long change,
                                   @Nullable final TrackedResource trackerEntry) {
        sendToPlayer(player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemResource(buf, itemResource);
            buf.writeLong(change);
            PacketUtil.writeTrackedResource(buf, trackerEntry);
        });
    }

    @Override
    public void sendResourceFilterSlotUpdate(final ServerPlayer player,
                                             final ResourceFilterContainer resourceFilterContainer,
                                             final int slotIndex,
                                             final int containerIndex) {
        sendToPlayer(player, PacketIds.RESOURCE_FILTER_SLOT_UPDATE, buf -> {
            buf.writeInt(slotIndex);
            resourceFilterContainer.writeToUpdatePacket(containerIndex, buf);
        });
    }

    @Override
    public void sendStorageInfoResponse(final ServerPlayer player, final UUID id, final StorageInfo storageInfo) {
        sendToPlayer(player, PacketIds.STORAGE_INFO_RESPONSE, bufToSend -> {
            bufToSend.writeUUID(id);
            bufToSend.writeLong(storageInfo.stored());
            bufToSend.writeLong(storageInfo.capacity());
        });
    }

    private static void sendToPlayer(final ServerPlayer playerEntity,
                                     final ResourceLocation id,
                                     final Consumer<FriendlyByteBuf> bufConsumer) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}
