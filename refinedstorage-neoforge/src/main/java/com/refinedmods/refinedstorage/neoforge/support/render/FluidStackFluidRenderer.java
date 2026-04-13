package com.refinedmods.refinedstorage.neoforge.support.render;

import com.refinedmods.refinedstorage.common.support.render.FluidRenderer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class FluidStackFluidRenderer implements FluidRenderer {
    private final LoadingCache<FluidResource, FluidStack> stackCache = CacheBuilder.newBuilder()
        .maximumSize(250)
        .build(new CacheLoader<>() {
            @Override
            public FluidStack load(final FluidResource resource) {
                return new FluidStack(
                    BuiltInRegistries.FLUID.wrapAsHolder(resource.fluid()),
                    FluidType.BUCKET_VOLUME,
                    resource.components()
                );
            }
        });

    private FluidStack getStack(final FluidResource resource) {
        return stackCache.getUnchecked(resource);
    }

    private int getTint(final FluidModel model, final FluidStack stack) {
        final FluidTintSource tintSource = model.fluidTintSource();
        if (tintSource == null) {
            return 0xFFFFFFFF;
        }
        return tintSource.colorAsStack(stack);
    }

    @Override
    public void render(final GuiGraphicsExtractor graphics, final int x, final int y,
                       final FluidResource resource) {
        final Minecraft minecraft = Minecraft.getInstance();
        final FluidStack stack = getStack(resource);
        final ModelManager modelManager = minecraft.getModelManager();
        final FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
        final FluidModel fluidModel = fluidStateModelSet.get(resource.fluid().defaultFluidState());
        final TextureAtlasSprite sprite = ClientPlatformUtil.getFluidSprite(fluidModel);
        final int tint = getTint(fluidModel, stack);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16, tint);
    }

    @Override
    public void render(final PoseStack poseStack, final SubmitNodeCollector nodes, final int light, final long seed,
                       final FluidResource resource) {
        final Minecraft minecraft = Minecraft.getInstance();
        final FluidStack stack = getStack(resource);
        final ModelManager modelManager = minecraft.getModelManager();
        final FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
        final FluidModel fluidModel = fluidStateModelSet.get(resource.fluid().defaultFluidState());
        final TextureAtlasSprite sprite = ClientPlatformUtil.getFluidSprite(fluidModel);
        final int tint = getTint(fluidModel, stack);
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
        final Minecraft minecraft = Minecraft.getInstance();
        return getStack(resource).getTooltipLines(
            Item.TooltipContext.EMPTY,
            ClientPlatformUtil.getClientPlayer(),
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
    }

    @Override
    public Component getDisplayName(final FluidResource resource) {
        return getStack(resource).getHoverName();
    }
}
