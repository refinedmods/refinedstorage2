package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridItemUpdatePacket {
    private final ItemResource resource;
    private final long amount;
    @Nullable
    private final TrackedResource trackedResource;

    public GridItemUpdatePacket(final ItemResource resource,
                                final long amount,
                                @Nullable final TrackedResource trackedResource) {
        this.resource = resource;
        this.amount = amount;
        this.trackedResource = trackedResource;
    }

    public static GridItemUpdatePacket decode(final FriendlyByteBuf buf) {
        return new GridItemUpdatePacket(
            PacketUtil.readItemResource(buf),
            buf.readLong(),
            PacketUtil.readTrackedResource(buf)
        );
    }

    public static void encode(final GridItemUpdatePacket packet, final FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, packet.resource);
        buf.writeLong(packet.amount);
        PacketUtil.writeTrackedResource(buf, packet.trackedResource);
    }

    public static void handle(final GridItemUpdatePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final GridItemUpdatePacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof ItemGridContainerMenu itemGrid) {
            itemGrid.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
        }
    }
}
