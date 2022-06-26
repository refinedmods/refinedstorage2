package com.refinedmods.refinedstorage2.platform.common.util;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static void writeItemResource(FriendlyByteBuf buf, ItemResource itemResource) {
        buf.writeVarInt(Item.getId(itemResource.item()));
        buf.writeNbt(itemResource.tag());
    }

    public static ItemResource readItemResource(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        CompoundTag nbt = buf.readNbt();
        return new ItemResource(Item.byId(id), nbt);
    }

    public static void writeItemResourceAmount(FriendlyByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> resourceAmount) {
        buf.writeVarInt(Item.getId(resourceAmount.getResource().item()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().tag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> readItemResourceAmount(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        long amount = buf.readLong();
        CompoundTag nbt = buf.readNbt();
        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new ItemResource(Item.byId(id), nbt),
                amount
        );
    }

    public static void writeFluidResource(FriendlyByteBuf buf, FluidResource itemResource) {
        buf.writeVarInt(Registry.FLUID.getId(itemResource.fluid()));
        buf.writeNbt(itemResource.tag());
    }

    public static FluidResource readFluidResource(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        CompoundTag nbt = buf.readNbt();
        return new FluidResource(Registry.FLUID.byId(id), nbt);
    }

    public static void writeFluidResourceAmount(FriendlyByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> resourceAmount) {
        buf.writeVarInt(Registry.FLUID.getId(resourceAmount.getResource().fluid()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().tag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> readFluidResourceAmount(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        long amount = buf.readLong();
        CompoundTag nbt = buf.readNbt();
        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new FluidResource(Registry.FLUID.byId(id), nbt),
                amount
        );
    }

    public static void writeTrackedResource(FriendlyByteBuf buf, TrackedResource trackedResource) {
        if (trackedResource == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(trackedResource.getTime());
            buf.writeUtf(trackedResource.getSourceName());
        }
    }

    public static TrackedResource readTrackedResource(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        long time = buf.readLong();
        String sourceName = buf.readUtf(32767);
        return new TrackedResource(sourceName, time);
    }
}
