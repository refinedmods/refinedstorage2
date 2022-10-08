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

public record FluidFilteredResource(FluidResource value, long amount) implements FilteredResource {
    private static final String TAG_AMOUNT = "amt";

    public static long getAmount(final CompoundTag tag) {
        return tag.getLong(TAG_AMOUNT);
    }

    @Override
    public void writeToPacket(final FriendlyByteBuf buf) {
        PacketUtil.writeFluidResource(buf, value);
        buf.writeLong(amount);
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag tag = FluidResource.toTag(value);
        tag.putLong(TAG_AMOUNT, amount);
        return tag;
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
    public String getAmount() {
        return Platform.INSTANCE.getBucketQuantityFormatter().formatWithUnits(amount);
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
