package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public interface FilteredResource {
    void writeToPacket(FriendlyByteBuf buf);

    CompoundTag toTag();

    void render(PoseStack poseStack, int x, int y, int z);

    Object getValue();

    ResourceType getType();

    List<Component> getTooltipLines(@Nullable Player player);
}
