package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Optional;
import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface PlatformStorageChannelType<T> extends StorageChannelType<T> {
    CompoundTag toTag(T resource, TrackedResource trackedResource);

    void fromTag(CompoundTag tag, BiConsumer<T, TrackedResource> acceptor);

    void toBuffer(T resource, FriendlyByteBuf buf);

    T fromBuffer(FriendlyByteBuf buf);

    Optional<GridResource> toGridResource(ResourceAmount<?> resourceAmount);

    boolean isGridResourceBelonging(GridResource gridResource);

    MutableComponent getTitle();

    ResourceLocation getTextureIdentifier();

    int getXTexture();

    int getYTexture();
}
