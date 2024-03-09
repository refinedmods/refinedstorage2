package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ResourceSlotUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final int slotIndex = buf.readInt();
        final boolean present = buf.readBoolean();
        if (present) {
            final ResourceLocation id = buf.readResourceLocation();
            PlatformApi.INSTANCE.getResourceTypeRegistry().get(id).ifPresent(
                type -> handle(type, buf, client, slotIndex)
            );
        } else {
            handle(client, containerMenu -> containerMenu.handleResourceSlotUpdate(slotIndex, null));
        }
    }

    private void handle(final ResourceType type,
                        final FriendlyByteBuf buf,
                        final Minecraft client,
                        final int slotIndex) {
        final ResourceKey resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        handle(client, containerMenu -> containerMenu.handleResourceSlotUpdate(slotIndex, new ResourceAmount(
            resource,
            amount
        )));
    }

    private void handle(final Minecraft client,
                        final Consumer<AbstractResourceContainerMenu> containerMenuConsumer) {
        client.execute(() -> {
            if (client.player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenuConsumer.accept(containerMenu);
            }
        });
    }
}
