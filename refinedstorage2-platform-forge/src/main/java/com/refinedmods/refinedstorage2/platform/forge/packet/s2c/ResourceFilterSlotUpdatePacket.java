package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotUpdatePacket {
    private final int slotIndex;
    private final FriendlyByteBuf buf;

    public ResourceFilterSlotUpdatePacket(int slotIndex, FriendlyByteBuf buf) {
        this.slotIndex = slotIndex;
        this.buf = buf;
    }

    public ResourceFilterSlotUpdatePacket(int slotIndex) {
        this.slotIndex = slotIndex;
        this.buf = null;
    }

    public static ResourceFilterSlotUpdatePacket decode(FriendlyByteBuf buf) {
        return new ResourceFilterSlotUpdatePacket(buf.readInt(), buf);
    }

    public static void encode(ResourceFilterSlotUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
    }

    public static void handle(ResourceFilterSlotUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof ResourceFilterableContainerMenu containerMenu) {
            containerMenu.readResourceFilterSlotUpdate(packet.slotIndex, packet.buf);
        }
        ctx.get().setPacketHandled(true);
    }
}
