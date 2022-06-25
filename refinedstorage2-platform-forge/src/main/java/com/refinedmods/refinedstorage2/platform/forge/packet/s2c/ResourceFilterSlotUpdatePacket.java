package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotUpdatePacket {
    private final int slotIndex;
    private final FriendlyByteBuf buf;
    private final ResourceFilterContainer resourceFilterContainer;

    public ResourceFilterSlotUpdatePacket(int slotIndex, FriendlyByteBuf buf) {
        this.slotIndex = slotIndex;
        this.buf = buf;
        this.resourceFilterContainer = null;
    }

    public ResourceFilterSlotUpdatePacket(int slotIndex, ResourceFilterContainer resourceFilterContainer) {
        this.slotIndex = slotIndex;
        this.buf = null;
        this.resourceFilterContainer = resourceFilterContainer;
    }

    public static ResourceFilterSlotUpdatePacket decode(FriendlyByteBuf buf) {
        return new ResourceFilterSlotUpdatePacket(buf.readInt(), buf);
    }

    public static void encode(ResourceFilterSlotUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        packet.resourceFilterContainer.writeToUpdatePacket(packet.slotIndex, buf);
    }

    public static void handle(ResourceFilterSlotUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        handle(packet);
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ResourceFilterSlotUpdatePacket packet) {
        if (Minecraft.getInstance().player.containerMenu instanceof ResourceFilterableContainerMenu containerMenu) {
            containerMenu.readResourceFilterSlotUpdate(packet.slotIndex, packet.buf);
        }
    }
}
