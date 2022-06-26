package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class FluidFilteredResource implements FilteredResource {
    private final FluidResource value;

    public FluidFilteredResource(FluidResource value) {
        this.value = value;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, value);
    }

    @Override
    public CompoundTag toTag() {
        return FluidResource.toTag(value);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int z) {
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
    public List<Component> getTooltipLines(Player player) {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(value);
    }
}
