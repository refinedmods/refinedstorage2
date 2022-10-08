package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface FilteredResource {
    void writeToPacket(FriendlyByteBuf buf);

    CompoundTag toTag();

    void render(PoseStack poseStack, int x, int y, int z);

    Object getValue();

    String getAmount();

    ResourceType getType();

    List<Component> getTooltipLines(@Nullable Player player);
}
