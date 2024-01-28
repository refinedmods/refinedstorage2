package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ResourceSlotAmountChangePacket(int slotIndex, long amount) implements CustomPacketPayload {
    public static ResourceSlotAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceSlotAmountChangePacket(buf.readInt(), buf.readLong());
    }

    public static void handle(final ResourceSlotAmountChangePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceSlotAmountChange(packet.slotIndex, packet.amount);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeLong(amount);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_SLOT_AMOUNT_CHANGE;
    }
}
