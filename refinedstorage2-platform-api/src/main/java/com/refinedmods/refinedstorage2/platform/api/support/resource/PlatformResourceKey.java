package com.refinedmods.refinedstorage2.platform.api.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.4")
public interface PlatformResourceKey extends ResourceKey {
    CompoundTag toTag();

    void toBuffer(FriendlyByteBuf buf);

    long getInterfaceExportLimit();

    ResourceType getResourceType();
}
