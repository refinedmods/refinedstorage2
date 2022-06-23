package com.refinedmods.refinedstorage2.platform.apiimpl.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FluidResourceType implements ResourceType<FluidResource> {
    private static final MutableComponent NAME = createTranslation("misc", "resource_type.fluid");

    public static final FluidResourceType INSTANCE = new FluidResourceType();

    private FluidResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<FluidResource> translate(ItemStack stack) {
        return Platform.INSTANCE.convertToFluid(stack);
    }

    @Override
    public void render(PoseStack poseStack, FluidResource value, int x, int y, int z) {
        Platform.INSTANCE.getFluidRenderer().render(poseStack, x, y, z, value);
    }

    @Override
    public FluidResource readFromPacket(FriendlyByteBuf buf) {
        return PacketUtil.readFluidResource(buf);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, FluidResource value) {
        PacketUtil.writeFluidResource(buf, value);
    }

    @Override
    public CompoundTag toTag(FluidResource value) {
        return FluidResource.toTag(value);
    }

    @Override
    public Optional<FluidResource> fromTag(CompoundTag tag) {
        return FluidResource.fromTag(tag);
    }

    @Override
    public List<Component> getTooltipLines(FluidResource value, Player player) {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(value);
    }
}
