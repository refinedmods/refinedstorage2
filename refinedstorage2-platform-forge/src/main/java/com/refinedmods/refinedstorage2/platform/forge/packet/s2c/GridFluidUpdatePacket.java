package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridFluidUpdatePacket {
    private final FluidResource resource;
    private final long amount;
    @Nullable
    private final TrackedResource trackedResource;

    public GridFluidUpdatePacket(final FluidResource resource,
                                 final long amount,
                                 @Nullable final TrackedResource trackedResource) {
        this.resource = resource;
        this.amount = amount;
        this.trackedResource = trackedResource;
    }

    public static GridFluidUpdatePacket decode(final FriendlyByteBuf buf) {
        return new GridFluidUpdatePacket(
            PacketUtil.readFluidResource(buf),
            buf.readLong(),
            PacketUtil.readTrackedResource(buf)
        );
    }

    public static void encode(final GridFluidUpdatePacket packet, final FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, packet.resource);
        buf.writeLong(packet.amount);
        PacketUtil.writeTrackedResource(buf, packet.trackedResource);
    }

    public static void handle(final GridFluidUpdatePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handle(packet));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final GridFluidUpdatePacket packet) {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof FluidGridContainerMenu fluidGrid) {
            fluidGrid.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
        }
    }
}
