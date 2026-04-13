package com.refinedmods.refinedstorage.common.api.support.resource;

import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.4")
public interface ResourceType {
    MapCodec<PlatformResourceKey> getMapCodec();

    StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> getStreamCodec();

    MutableComponent getTitle();

    Identifier getSprite();

    long normalizeAmount(double amount);

    double getDisplayAmount(long amount);

    long getInterfaceExportLimit();

    GridOperations createGridOperations(RootStorage rootStorage, Actor actor);
}
