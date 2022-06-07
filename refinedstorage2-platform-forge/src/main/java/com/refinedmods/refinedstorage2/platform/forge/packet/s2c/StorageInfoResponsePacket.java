package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class StorageInfoResponsePacket {
    private final UUID id;
    private final long stored;
    private final long capacity;

    public StorageInfoResponsePacket(UUID id, long stored, long capacity) {
        this.id = id;
        this.stored = stored;
        this.capacity = capacity;
    }

    public static StorageInfoResponsePacket decode(FriendlyByteBuf buf) {
        return new StorageInfoResponsePacket(buf.readUUID(), buf.readLong(), buf.readLong());
    }

    public static void encode(StorageInfoResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(StorageInfoResponsePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handle(packet));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(StorageInfoResponsePacket packet) {
        Level level = Minecraft.getInstance().player.level;
        ((ClientStorageRepository) PlatformApi.INSTANCE.getStorageRepository(level)).setInfo(packet.id, packet.stored, packet.capacity);
    }
}
