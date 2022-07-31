package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public record FluidFilteredResource(FluidResource value) implements FilteredResource {
    @Override
    public void writeToPacket(final FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, value);
    }

    @Override
    public CompoundTag toTag() {
        return FluidResource.toTag(value);
    }

    @Override
    public void render(final PoseStack poseStack, final int x, final int y, final int z) {
        Platform.INSTANCE.getFluidRenderer().render(poseStack, x, y, z, value);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public ResourceType getType() {
        return FluidResourceType.INSTANCE;
    }

    @Override
    public List<Component> getTooltipLines(@Nullable final Player player) {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(value);
    }
}
