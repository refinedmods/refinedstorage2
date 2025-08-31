package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record AutocraftingRequestPacket(UUID id,
                                        PlatformResourceKey resource,
                                        long amount,
                                        boolean notifyPlayer) implements CustomPacketPayload {
    public static final Type<AutocraftingRequestPacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingRequestPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AutocraftingRequestPacket::id,
            ResourceCodecs.STREAM_CODEC, AutocraftingRequestPacket::resource,
            ByteBufCodecs.VAR_LONG, AutocraftingRequestPacket::amount,
            ByteBufCodecs.BOOL, AutocraftingRequestPacket::notifyPlayer,
            AutocraftingRequestPacket::new
        );

    public static void handle(final AutocraftingRequestPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        if (player.containerMenu instanceof PreviewProvider provider) {
            final PlayerActor playerActor = new PlayerActor(player);
            final var taskId = provider.startTask(packet.resource, packet.amount, playerActor, packet.notifyPlayer,
                new TimeoutableCancellationToken());
            S2CPackets.sendAutocraftingResponse((ServerPlayer) player, packet.id, taskId.isPresent());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
