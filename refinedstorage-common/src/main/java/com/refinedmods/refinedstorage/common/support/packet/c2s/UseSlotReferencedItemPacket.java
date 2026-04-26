package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.UsablePlayerSlotReferencedItem;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record UseSlotReferencedItemPacket(PlayerSlotReference playerSlotReference) implements CustomPacketPayload {
    public static final Type<UseSlotReferencedItemPacket> PACKET_TYPE = new Type<>(
        createIdentifier("use_slot_referenced_item")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UseSlotReferencedItemPacket> STREAM_CODEC = StreamCodec
        .composite(
            PlayerSlotReference.STREAM_CODEC, UseSlotReferencedItemPacket::playerSlotReference,
            UseSlotReferencedItemPacket::new
        );

    public static void handle(final UseSlotReferencedItemPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        final ItemStack stack = packet.playerSlotReference.get(player);
        if (!(stack.getItem() instanceof UsablePlayerSlotReferencedItem handlerItem)) {
            return;
        }
        handlerItem.use((ServerPlayer) player, stack, packet.playerSlotReference);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
