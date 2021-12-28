package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ResourceType<T> {
    Component getName();

    Optional<T> translate(ItemStack stack);

    void render(PoseStack poseStack, T value, int x, int y, int z);

    ResourceLocation getId();

    T readFromPacket(FriendlyByteBuf buf);

    void writeToPacket(FriendlyByteBuf buf, T value);

    CompoundTag toTag(T value);

    Optional<T> fromTag(CompoundTag tag);

    List<Component> getTooltipLines(T value, Player player);
}
