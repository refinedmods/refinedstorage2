package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.ClientProxy;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ResourceSlotUpdatePacket<T> {
    private final int slotIndex;
    @Nullable
    private final ResourceInstance<T> resourceInstance;
    @Nullable
    private final ResourceLocation storageChannelTypeId;

    public ResourceSlotUpdatePacket(final int slotIndex,
                                    @Nullable final ResourceInstance<T> resourceInstance,
                                    @Nullable final ResourceLocation storageChannelTypeId) {
        this.slotIndex = slotIndex;
        this.resourceInstance = resourceInstance;
        this.storageChannelTypeId = storageChannelTypeId;
    }

    public static ResourceSlotUpdatePacket<?> decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final boolean present = buf.readBoolean();
        if (!present) {
            return new ResourceSlotUpdatePacket<>(slotIndex, null, null);
        }
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId).map(
            storageChannelType -> decode(buf, slotIndex, storageChannelType)
        ).orElseGet(() -> new ResourceSlotUpdatePacket<>(slotIndex, null, null));
    }

    private static <T> ResourceSlotUpdatePacket<T> decode(final FriendlyByteBuf buf,
                                                          final int slotIndex,
                                                          final PlatformStorageChannelType<T> type) {
        final T resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        final ResourceAmount<T> resourceAmount = new ResourceAmount<>(resource, amount);
        final ResourceInstance<T> resourceInstance = new ResourceInstance<>(resourceAmount, type);
        return new ResourceSlotUpdatePacket<>(slotIndex, resourceInstance, null);
    }

    public static <T> void encode(final ResourceSlotUpdatePacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        final boolean present = packet.resourceInstance != null && packet.storageChannelTypeId != null;
        buf.writeBoolean(present);
        if (present) {
            buf.writeResourceLocation(packet.storageChannelTypeId);
            packet.resourceInstance.getStorageChannelType().toBuffer(packet.resourceInstance.getResource(), buf);
            buf.writeLong(packet.resourceInstance.getAmount());
        }
    }

    public static <T> void handle(final ResourceSlotUpdatePacket<T> packet,
                                  final Supplier<NetworkEvent.Context> ctx) {
        ClientProxy.getPlayer().ifPresent(player -> handle(player, packet));
        ctx.get().setPacketHandled(true);
    }

    private static <T> void handle(final Player player, final ResourceSlotUpdatePacket<T> packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceSlotUpdate(packet.slotIndex, packet.resourceInstance);
        }
    }
}
