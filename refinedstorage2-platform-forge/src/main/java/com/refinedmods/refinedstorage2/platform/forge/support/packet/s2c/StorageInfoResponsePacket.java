package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record StorageInfoResponsePacket(UUID storageId, long stored, long capacity) implements CustomPacketPayload {
    public static StorageInfoResponsePacket decode(final FriendlyByteBuf buf) {
        return new StorageInfoResponsePacket(buf.readUUID(), buf.readLong(), buf.readLong());
    }

    public static void handle(final StorageInfoResponsePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            final ClientStorageRepository storageRepository = (ClientStorageRepository)
                PlatformApi.INSTANCE.getStorageRepository(player.level());
            storageRepository.setInfo(packet.storageId, packet.stored, packet.capacity);
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeUUID(storageId);
        buf.writeLong(stored);
        buf.writeLong(capacity);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.STORAGE_INFO_RESPONSE;
    }
}
