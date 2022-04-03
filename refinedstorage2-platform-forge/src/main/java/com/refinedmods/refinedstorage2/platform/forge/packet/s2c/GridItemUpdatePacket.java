package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridItemUpdatePacket {
    private final ItemResource resource;
    private final long amount;
    private final TrackedResource trackedResource;

    public GridItemUpdatePacket(ItemResource resource, long amount, TrackedResource trackedResource) {
        this.resource = resource;
        this.amount = amount;
        this.trackedResource = trackedResource;
    }

    public static GridItemUpdatePacket decode(FriendlyByteBuf buf) {
        return new GridItemUpdatePacket(
                PacketUtil.readItemResource(buf),
                buf.readLong(),
                PacketUtil.readTrackedResource(buf)
        );
    }

    public static void encode(GridItemUpdatePacket packet, FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, packet.resource);
        buf.writeLong(packet.amount);
        PacketUtil.writeTrackedResource(buf, packet.trackedResource);
    }

    public static void handle(GridItemUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handle(packet));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(GridItemUpdatePacket packet) {
        AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
        if (menu instanceof ItemGridContainerMenu itemGrid) {
            itemGrid.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
        }
    }
}
