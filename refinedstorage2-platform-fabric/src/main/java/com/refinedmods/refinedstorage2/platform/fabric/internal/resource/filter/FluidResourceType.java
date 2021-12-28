package com.refinedmods.refinedstorage2.platform.fabric.internal.resource.filter;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.impl.transfer.context.InitialContentsContainerItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FluidResourceType implements ResourceType<FluidResource> {
    private static final ResourceLocation ID = Rs2Mod.createIdentifier("fluid_resource_type");
    private static final TranslatableComponent NAME = Rs2Mod.createTranslation("misc", "resource_type.fluid");

    public static final FluidResourceType INSTANCE = new FluidResourceType();

    private FluidResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<FluidResource> translate(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : translateNonEmpty(stack);
    }

    private Optional<FluidResource> translateNonEmpty(ItemStack stack) {
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(
                stack,
                new InitialContentsContainerItemContext(ItemVariant.of(stack), 1)
        );
        return Optional
                .ofNullable(StorageUtil.findExtractableResource(storage, null))
                .map(FluidResource::ofFluidVariant);
    }

    @Override
    public void render(PoseStack poseStack, FluidResource value, int x, int y, int z) {
        ScreenUtil.renderFluid(poseStack, x, y, z, value.toFluidVariant());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
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
        TooltipFlag.Default flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        return FluidVariantRendering.getTooltip(value.toFluidVariant(), flag);
    }
}
