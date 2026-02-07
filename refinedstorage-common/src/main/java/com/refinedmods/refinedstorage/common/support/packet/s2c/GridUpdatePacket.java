package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.storage.TrackedResourceCodecs;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record GridUpdatePacket(
    PlatformResourceKey resource,
    long amount,
    Optional<TrackedResource> trackedResource
) implements CustomPacketPayload {
    public static final Type<GridUpdatePacket> PACKET_TYPE = new Type<>(createIdentifier("grid_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceCodecs.STREAM_CODEC, GridUpdatePacket::resource,
        ByteBufCodecs.VAR_LONG, GridUpdatePacket::amount,
        TrackedResourceCodecs.OPTIONAL_STREAM_CODEC, GridUpdatePacket::trackedResource,
        GridUpdatePacket::new
    );

    public static void handle(final GridUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            containerMenu.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource.orElse(null));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
