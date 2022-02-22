package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridFluidUpdatePacket {
    private final FluidResource resource;
    private final long amount;
    private final StorageTracker.Entry trackerEntry;

    public GridFluidUpdatePacket(FluidResource resource, long amount, StorageTracker.Entry trackerEntry) {
        this.resource = resource;
        this.amount = amount;
        this.trackerEntry = trackerEntry;
    }

    public static GridFluidUpdatePacket decode(FriendlyByteBuf buf) {
        return new GridFluidUpdatePacket(
                PacketUtil.readFluidResource(buf),
                buf.readLong(),
                PacketUtil.readTrackerEntry(buf)
        );
    }

    public static void encode(GridFluidUpdatePacket packet, FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, packet.resource);
        buf.writeLong(packet.amount);
        PacketUtil.writeTrackerEntry(buf, packet.trackerEntry);
    }

    public static void handle(GridFluidUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handle(packet));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(GridFluidUpdatePacket packet) {
        AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
        if (menu instanceof FluidGridContainerMenu fluidGrid) {
            fluidGrid.onResourceUpdate(packet.resource, packet.amount, packet.trackerEntry);
        }
    }
}
