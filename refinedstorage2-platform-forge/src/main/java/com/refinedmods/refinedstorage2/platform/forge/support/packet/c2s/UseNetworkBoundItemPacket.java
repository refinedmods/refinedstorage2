package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.energy.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static java.util.Objects.requireNonNull;

public record UseNetworkBoundItemPacket(@Nullable SlotReference slotReference) implements CustomPacketPayload {
    public static UseNetworkBoundItemPacket decode(final FriendlyByteBuf buf) {
        return new UseNetworkBoundItemPacket(PlatformApi.INSTANCE.getSlotReference(buf).orElse(null));
    }

    public static void handle(final UseNetworkBoundItemPacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> doHandle(packet, player)));
    }

    private static void doHandle(final UseNetworkBoundItemPacket packet, final Player player) {
        if (packet.slotReference == null) {
            return;
        }
        packet.slotReference.resolve(player).ifPresent(stack -> {
            if (!(stack.getItem() instanceof AbstractNetworkBoundEnergyItem networkBoundItem)) {
                return;
            }
            final NetworkBoundItemSession sess = PlatformApi.INSTANCE.getNetworkBoundItemHelper().openSession(
                stack,
                (ServerPlayer) player,
                packet.slotReference
            );
            networkBoundItem.use((ServerPlayer) player, packet.slotReference, sess);
        });
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        PlatformApi.INSTANCE.writeSlotReference(requireNonNull(slotReference), buf);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.USE_NETWORK_BOUND_ITEM;
    }
}
