package com.refinedmods.refinedstorage2.platform.common.util;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static void writeItemResource(final FriendlyByteBuf buf, final ItemResource itemResource) {
        buf.writeVarInt(Item.getId(itemResource.item()));
        buf.writeNbt(itemResource.tag());
    }

    public static ItemResource readItemResource(FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final CompoundTag nbt = buf.readNbt();
        return new ItemResource(Item.byId(id), nbt);
    }

    public static void writeItemResourceAmount(final FriendlyByteBuf buf, final com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> resourceAmount) {
        buf.writeVarInt(Item.getId(resourceAmount.getResource().item()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().tag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> readItemResourceAmount(final FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final long amount = buf.readLong();
        final CompoundTag nbt = buf.readNbt();
        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new ItemResource(Item.byId(id), nbt),
                amount
        );
    }

    public static void writeFluidResource(final FriendlyByteBuf buf, final FluidResource itemResource) {
        buf.writeVarInt(Registry.FLUID.getId(itemResource.fluid()));
        buf.writeNbt(itemResource.tag());
    }

    public static FluidResource readFluidResource(final FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final CompoundTag nbt = buf.readNbt();
        return new FluidResource(Registry.FLUID.byId(id), nbt);
    }

    public static void writeFluidResourceAmount(final FriendlyByteBuf buf, final com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> resourceAmount) {
        buf.writeVarInt(Registry.FLUID.getId(resourceAmount.getResource().fluid()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().tag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> readFluidResourceAmount(final FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final long amount = buf.readLong();
        final CompoundTag nbt = buf.readNbt();
        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new FluidResource(Registry.FLUID.byId(id), nbt),
                amount
        );
    }

    public static void writeTrackedResource(final FriendlyByteBuf buf, @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(trackedResource.getTime());
            buf.writeUtf(trackedResource.getSourceName());
        }
    }

    @Nullable
    public static TrackedResource readTrackedResource(final FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        final long time = buf.readLong();
        final String sourceName = buf.readUtf();
        return new TrackedResource(sourceName, time);
    }
}
