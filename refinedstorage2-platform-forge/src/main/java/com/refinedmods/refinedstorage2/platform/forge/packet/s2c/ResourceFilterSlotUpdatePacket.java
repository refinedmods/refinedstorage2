package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotUpdatePacket {
    private final int slotIndex;
    private final int containerIndex;
    @Nullable
    private final FriendlyByteBuf buf;
    @Nullable
    private final ResourceFilterContainer resourceFilterContainer;

    public ResourceFilterSlotUpdatePacket(final int slotIndex, final FriendlyByteBuf buf) {
        this.slotIndex = slotIndex;
        this.containerIndex = -1;
        this.buf = buf;
        this.resourceFilterContainer = null;
    }

    public ResourceFilterSlotUpdatePacket(final int slotIndex,
                                          final int containerIndex,
                                          final ResourceFilterContainer resourceFilterContainer) {
        this.slotIndex = slotIndex;
        this.containerIndex = containerIndex;
        this.buf = null;
        this.resourceFilterContainer = resourceFilterContainer;
    }

    public static ResourceFilterSlotUpdatePacket decode(final FriendlyByteBuf buf) {
        return new ResourceFilterSlotUpdatePacket(buf.readInt(), buf);
    }

    public static void encode(final ResourceFilterSlotUpdatePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        Objects.requireNonNull(packet.resourceFilterContainer).writeToUpdatePacket(packet.containerIndex, buf);
    }

    public static void handle(final ResourceFilterSlotUpdatePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ClientProxy.getPlayer().ifPresent(player -> handle(player, packet));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final ResourceFilterSlotUpdatePacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractResourceFilterContainerMenu resourceFilterable) {
            resourceFilterable.readResourceFilterSlotUpdate(packet.slotIndex, Objects.requireNonNull(packet.buf));
        }
    }
}
