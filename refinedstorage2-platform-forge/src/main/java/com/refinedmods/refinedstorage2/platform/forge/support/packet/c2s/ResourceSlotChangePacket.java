package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ResourceSlotChangePacket(int slotIndex, boolean tryAlternatives) implements CustomPacketPayload {
    public static ResourceSlotChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceSlotChangePacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(final ResourceSlotChangePacket packet,
                              final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceSlotChange(packet.slotIndex, packet.tryAlternatives);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeBoolean(tryAlternatives);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_SLOT_CHANGE;
    }
}
