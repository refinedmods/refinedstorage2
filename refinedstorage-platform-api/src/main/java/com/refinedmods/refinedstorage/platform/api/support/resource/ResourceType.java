package com.refinedmods.refinedstorage.platform.api.support.resource;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.4")
public interface ResourceType {
    MapCodec<PlatformResourceKey> getMapCodec();

    Codec<PlatformResourceKey> getCodec();

    StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> getStreamCodec();

    MutableComponent getTitle();

    ResourceLocation getTextureIdentifier();

    int getXTexture();

    int getYTexture();

    long normalizeAmount(double amount);

    double getDisplayAmount(long amount);

    Optional<GridResource> toGridResource(ResourceAmount resourceAmount);

    boolean isGridResourceBelonging(GridResource gridResource);

    long getInterfaceExportLimit();

    GridOperations createGridOperations(StorageChannel storageChannel, Actor actor);
}
