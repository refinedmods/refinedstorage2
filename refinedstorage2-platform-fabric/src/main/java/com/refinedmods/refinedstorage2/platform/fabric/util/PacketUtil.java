package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static void writeItemResource(PacketByteBuf buf, ItemResource itemResource) {
        buf.writeVarInt(Item.getRawId(itemResource.getItem()));
        buf.writeNbt(itemResource.getTag());
    }

    public static ItemResource readItemResource(PacketByteBuf buf) {
        int id = buf.readVarInt();
        NbtCompound nbt = buf.readNbt();

        return new ItemResource(Item.byRawId(id), nbt);
    }

    public static void writeItemResourceAmount(PacketByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> resourceAmount) {
        buf.writeVarInt(Item.getRawId(resourceAmount.getResource().getItem()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().getTag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> readItemResourceAmount(PacketByteBuf buf) {
        int id = buf.readVarInt();
        long amount = buf.readLong();
        NbtCompound nbt = buf.readNbt();

        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new ItemResource(Item.byRawId(id), nbt),
                amount
        );
    }

    public static void writeFluidResource(PacketByteBuf buf, FluidResource itemResource) {
        buf.writeVarInt(Registry.FLUID.getRawId(itemResource.getFluid()));
        buf.writeNbt(itemResource.getTag());
    }

    public static FluidResource readFluidResource(PacketByteBuf buf) {
        int id = buf.readVarInt();
        NbtCompound nbt = buf.readNbt();

        return new FluidResource(Registry.FLUID.get(id), nbt);
    }

    public static void writeFluidResourceAmount(PacketByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> resourceAmount) {
        buf.writeVarInt(Registry.FLUID.getRawId(resourceAmount.getResource().getFluid()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().getTag());
    }

    public static com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> readFluidResourceAmount(PacketByteBuf buf) {
        int id = buf.readVarInt();
        long amount = buf.readLong();
        NbtCompound nbt = buf.readNbt();

        return new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                new FluidResource(Registry.FLUID.get(id), nbt),
                amount
        );
    }

    public static void writeTrackerEntry(PacketByteBuf buf, Optional<StorageTracker.Entry> entry) {
        if (!entry.isPresent()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(entry.get().time());
            buf.writeString(entry.get().name());
        }
    }

    public static StorageTracker.Entry readTrackerEntry(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        long time = buf.readLong();
        String name = buf.readString(32767);
        return new StorageTracker.Entry(time, name);
    }
}
