package com.refinedmods.refinedstorage2.platform.common.util;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static ItemResource readItemResource(final FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final CompoundTag nbt = buf.readNbt();
        return new ItemResource(Item.byId(id), nbt);
    }

    public static PlatformResourceKey readFluidResource(final FriendlyByteBuf buf) {
        final int id = buf.readVarInt();
        final CompoundTag nbt = buf.readNbt();
        return new FluidResource(BuiltInRegistries.FLUID.byId(id), nbt);
    }

    public static void writeTrackedResource(final FriendlyByteBuf buf,
                                            @Nullable final TrackedResource trackedResource) {
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
