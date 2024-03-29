package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.energy.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class UseNetworkBoundItemPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final Optional<SlotReference> slotRefMaybe = PlatformApi.INSTANCE.getSlotReference(buf);
        server.execute(() -> slotRefMaybe.ifPresent(slotReference -> slotReference.resolve(player).ifPresent(stack -> {
            if (!(stack.getItem() instanceof AbstractNetworkBoundEnergyItem networkBoundItem)) {
                return;
            }
            final NetworkBoundItemSession sess = PlatformApi.INSTANCE.getNetworkBoundItemHelper().openSession(
                stack,
                player,
                slotReference
            );
            networkBoundItem.use(player, slotReference, sess);
        })));
    }
}
