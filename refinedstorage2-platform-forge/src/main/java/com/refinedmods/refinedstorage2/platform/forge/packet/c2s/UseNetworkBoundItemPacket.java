package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.item.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class UseNetworkBoundItemPacket {
    private final SlotReference slotReference;

    public UseNetworkBoundItemPacket(final SlotReference slotReference) {
        this.slotReference = slotReference;
    }

    public static UseNetworkBoundItemPacket decode(final FriendlyByteBuf buf) {
        return new UseNetworkBoundItemPacket(PlatformApi.INSTANCE.createSlotReference(buf));
    }

    public static void encode(final UseNetworkBoundItemPacket packet, final FriendlyByteBuf buf) {
        packet.slotReference.writeToBuf(buf);
    }

    public static void handle(final UseNetworkBoundItemPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final UseNetworkBoundItemPacket packet, final ServerPlayer player) {
        packet.slotReference.resolve(player).ifPresent(stack -> {
            if (!(stack.getItem() instanceof AbstractNetworkBoundEnergyItem networkBoundItem)) {
                return;
            }
            final NetworkBoundItemSession sess = PlatformApi.INSTANCE.getNetworkBoundItemHelper().openSession(
                stack,
                player,
                packet.slotReference
            );
            networkBoundItem.use(player, packet.slotReference, sess);
        });
    }
}
