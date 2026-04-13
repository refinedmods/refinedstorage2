package com.refinedmods.refinedstorage.fabric.support.render;

import com.refinedmods.refinedstorage.common.support.render.FluidRenderer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;
import com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil;

import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;

public class FluidVariantFluidRenderer implements FluidRenderer {
    private final LoadingCache<FluidResource, FluidVariant> stackCache = CacheBuilder.newBuilder()
        .maximumSize(250)
        .build(new CacheLoader<>() {
            @Override
            public FluidVariant load(final FluidResource resource) {
                return VariantUtil.toFluidVariant(resource);
            }
        });

    private FluidVariant getVariant(final FluidResource resource) {
        return stackCache.getUnchecked(resource);
    }

    private int getTint(final FluidVariant variant) {
        final int tint = FluidVariantRendering.getColor(variant);
        if (tint == -1) {
            // See https://github.com/FabricMC/fabric-api/issues/5305
            if (variant.getFluid().isSame(Fluids.WATER)) {
                return 0xFF3F76E4;
            }
            return 0xFFFFFFFF;
        }
        return tint;
    }

    @Override
    public void render(final GuiGraphicsExtractor graphics, final int x, final int y,
                       final FluidResource resource) {
        final FluidVariant variant = getVariant(resource);
        final TextureAtlasSprite sprite = ClientPlatformUtil.getFluidSprite(resource);
        final int tint = getTint(variant);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16, tint);
    }

    @Override
    public void render(final PoseStack poseStack, final SubmitNodeCollector nodes, final int light, final long seed,
                       final FluidResource resource) {
        final FluidVariant variant = getVariant(resource);
        final TextureAtlasSprite sprite = ClientPlatformUtil.getFluidSprite(resource);
        final int tint = getTint(variant);
        nodes.submitCustomGeometry(poseStack, RenderTypes.entitySolid(sprite.atlasLocation()), (pose, buffer) -> {
            final float scale = 0.3F;
            final var x0 = -scale / 2;
            final var y0 = scale / 2;
            final var x1 = scale / 2;
            final var y1 = -scale / 2;
            buffer.addVertex(pose, x0, y1, 0)
                .setColor(tint)
                .setUv(sprite.getU0(), sprite.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 0, 1);
            buffer.addVertex(pose, x1, y1, 0)
                .setColor(tint)
                .setUv(sprite.getU1(), sprite.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 0, 1);
            buffer.addVertex(pose, x1, y0, 0)
                .setColor(tint)
                .setUv(sprite.getU1(), sprite.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 0, 1);
            buffer.addVertex(pose, x0, y0, 0)
                .setColor(tint)
                .setUv(sprite.getU0(), sprite.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 0, 1);
        });
    }

    @Override
    public List<Component> getTooltip(final FluidResource resource) {
        return FluidVariantRendering.getTooltip(
            getVariant(resource),
            Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
    }

    @Override
    public Component getDisplayName(final FluidResource resource) {
        return FluidVariantAttributes.getName(getVariant(resource));
    }
}
