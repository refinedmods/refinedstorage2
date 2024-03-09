package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceRendering;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public class ItemResourceRendering implements ResourceRendering {
    public static final Matrix4f IN_WORLD_SCALE = new Matrix4f().scale(0.3F, 0.3F, 0.001f);
    private final Map<ItemResource, ItemStack> stackCache = new HashMap<>();

    private ItemStack getStack(final ItemResource itemResource) {
        return stackCache.computeIfAbsent(itemResource, ItemResource::toItemStack);
    }

    @Override
    public String getDisplayedAmount(final long amount, final boolean withUnits) {
        if (!withUnits) {
            return AmountFormatting.format(amount);
        }
        if (amount == 1) {
            return "";
        }
        return AmountFormatting.formatWithUnits(amount);
    }

    @Override
    public Component getDisplayName(final ResourceKey resource) {
        if (!(resource instanceof ItemResource itemResource)) {
            return Component.empty();
        }
        return getStack(itemResource).getHoverName();
    }

    @Override
    public List<Component> getTooltip(final ResourceKey resource) {
        if (!(resource instanceof ItemResource itemResource)) {
            return Collections.emptyList();
        }
        final Minecraft minecraft = Minecraft.getInstance();
        return getStack(itemResource).getTooltipLines(
            minecraft.player,
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
    }

    @Override
    public void render(final ResourceKey resource, final GuiGraphics graphics, final int x, final int y) {
        if (!(resource instanceof ItemResource itemResource)) {
            return;
        }
        final ItemStack stack = getStack(itemResource);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    @Override
    public void render(final ResourceKey resource,
                       final PoseStack poseStack,
                       final MultiBufferSource renderTypeBuffer,
                       final int light,
                       final Level level) {
        if (!(resource instanceof ItemResource itemResource)) {
            return;
        }
        final ItemStack itemStack = getStack(itemResource);
        poseStack.mulPoseMatrix(IN_WORLD_SCALE);
        poseStack.last().normal().rotateX(Mth.DEG_TO_RAD * -45f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
            itemStack,
            ItemDisplayContext.GUI,
            light,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            renderTypeBuffer,
            level,
            0
        );
    }
}
