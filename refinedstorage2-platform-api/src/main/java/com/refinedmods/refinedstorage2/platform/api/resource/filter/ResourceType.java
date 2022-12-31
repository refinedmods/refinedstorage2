package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface ResourceType {
    Component getName();

    Optional<FilteredResource> translate(ItemStack stack);

    Optional<FilteredResource> fromTag(CompoundTag tag);

    FilteredResource fromPacket(FriendlyByteBuf buf);
}
