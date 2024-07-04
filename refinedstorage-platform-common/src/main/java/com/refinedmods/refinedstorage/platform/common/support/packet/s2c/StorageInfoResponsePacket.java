package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.common.storage.ClientStorageRepository;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record StorageInfoResponsePacket(UUID storageId, long stored, long capacity) implements CustomPacketPayload {
    public static final Type<StorageInfoResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("storage_info_response")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageInfoResponsePacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StorageInfoResponsePacket::storageId,
            ByteBufCodecs.VAR_LONG, StorageInfoResponsePacket::stored,
            ByteBufCodecs.VAR_LONG, StorageInfoResponsePacket::capacity,
            StorageInfoResponsePacket::new
        );

    public static void handle(final StorageInfoResponsePacket packet) {
        final ClientStorageRepository storageRepository =
            (ClientStorageRepository) PlatformApi.INSTANCE.getClientStorageRepository();
        storageRepository.setInfo(packet.storageId, packet.stored, packet.capacity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
