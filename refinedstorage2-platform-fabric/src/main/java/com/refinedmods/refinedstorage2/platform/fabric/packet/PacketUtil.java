package com.refinedmods.refinedstorage2.platform.fabric.packet;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static void writeItemResource(FriendlyByteBuf buf, ItemResource itemResource) {
        buf.writeVarInt(Item.getId(itemResource.getItem()));
        buf.writeNbt(itemResource.getTag());
    }

    public static ItemResource readItemResource(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        CompoundTag nbt = buf.readNbt();
        return new ItemResource(Item.byId(id), nbt);
    }

    public static void writeItemResourceAmount(FriendlyByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<ItemResource> resourceAmount) {
        buf.writeVarInt(Item.getId(resourceAmount.getResource().getItem()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().getTag());
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
        buf.writeVarInt(Registry.FLUID.getId(itemResource.getFluid()));
        buf.writeNbt(itemResource.getTag());
    }

    public static FluidResource readFluidResource(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        CompoundTag nbt = buf.readNbt();
        return new FluidResource(Registry.FLUID.byId(id), nbt);
    }

    public static void writeFluidResourceAmount(FriendlyByteBuf buf, com.refinedmods.refinedstorage2.api.resource.ResourceAmount<FluidResource> resourceAmount) {
        buf.writeVarInt(Registry.FLUID.getId(resourceAmount.getResource().getFluid()));
        buf.writeLong(resourceAmount.getAmount());
        buf.writeNbt(resourceAmount.getResource().getTag());
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

    public static void writeTrackerEntry(FriendlyByteBuf buf, StorageTracker.Entry entry) {
        if (entry == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(entry.time());
            buf.writeUtf(entry.name());
        }
    }

    public static StorageTracker.Entry readTrackerEntry(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        long time = buf.readLong();
        String name = buf.readUtf(32767);
        return new StorageTracker.Entry(time, name);
    }
}
