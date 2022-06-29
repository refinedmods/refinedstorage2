package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
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
        handle(packet);
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceFilterSlotUpdatePacket packet) {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractResourceFilterContainerMenu resourceFilterable) {
            resourceFilterable.readResourceFilterSlotUpdate(packet.slotIndex, Objects.requireNonNull(packet.buf));
        }
    }
}
