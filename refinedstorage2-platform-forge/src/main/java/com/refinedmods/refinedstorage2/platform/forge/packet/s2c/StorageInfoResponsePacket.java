package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.util.ClientProxy;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class StorageInfoResponsePacket {
    private final UUID id;
    private final long stored;
    private final long capacity;

    public StorageInfoResponsePacket(final UUID id, final long stored, final long capacity) {
        this.id = id;
        this.stored = stored;
        this.capacity = capacity;
    }

    public static StorageInfoResponsePacket decode(final FriendlyByteBuf buf) {
        return new StorageInfoResponsePacket(buf.readUUID(), buf.readLong(), buf.readLong());
    }

    public static void encode(final StorageInfoResponsePacket packet, final FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(final StorageInfoResponsePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final StorageInfoResponsePacket packet) {
        final ClientStorageRepository storageRepository =
            (ClientStorageRepository) PlatformApi.INSTANCE.getStorageRepository(player.level());
        storageRepository.setInfo(packet.id, packet.stored, packet.capacity);
    }
}
