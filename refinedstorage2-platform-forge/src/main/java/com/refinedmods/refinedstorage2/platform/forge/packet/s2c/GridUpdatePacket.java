package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class GridUpdatePacket<T> {
    private final PlatformStorageChannelType<T> storageChannelType;
    private final ResourceLocation storageChannelTypeId;
    private final T resource;
    private final long amount;
    @Nullable
    private final TrackedResource trackedResource;

    public GridUpdatePacket(
        final PlatformStorageChannelType<T> storageChannelType,
        final ResourceLocation storageChannelTypeId,
        final T resource,
        final long amount,
        @Nullable final TrackedResource trackedResource
    ) {
        this.storageChannelType = storageChannelType;
        this.storageChannelTypeId = storageChannelTypeId;
        this.resource = resource;
        this.amount = amount;
        this.trackedResource = trackedResource;
    }

    @SuppressWarnings("unchecked")
    public static GridUpdatePacket<?> decode(final FriendlyByteBuf buf) {
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        final PlatformStorageChannelType<?> storageChannelType = PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .get(storageChannelTypeId)
            .orElseThrow();
        final Object resource = storageChannelType.fromBuffer(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
        return new GridUpdatePacket<>(
            (PlatformStorageChannelType<? super Object>) storageChannelType,
            storageChannelTypeId,
            resource,
            amount,
            trackedResource
        );
    }

    public static <T> void encode(final GridUpdatePacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.storageChannelTypeId);
        packet.storageChannelType.toBuffer(packet.resource, buf);
        buf.writeLong(packet.amount);
        PacketUtil.writeTrackedResource(buf, packet.trackedResource);
    }

    public static <T> void handle(final GridUpdatePacket<T> packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static <T> void handle(final Player player, final GridUpdatePacket<T> packet) {
        if (player.containerMenu instanceof GridContainerMenu containerMenu) {
            containerMenu.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
        }
    }
}
