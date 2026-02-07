package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import java.util.Collections;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.formatWithUnits;
import static java.util.Objects.requireNonNull;

public enum ItemResourceRendering implements ResourceRendering {
    INSTANCE;

    public static final Matrix4f IN_WORLD_SCALE = new Matrix4f().scale(0.3F, 0.3F, 0.001F);

    private final LoadingCache<ItemResource, ItemStack> stackCache = CacheBuilder.newBuilder()
        .maximumSize(250)
        .build(new CacheLoader<>() {
            @Override
            public ItemStack load(final ItemResource resource) {
                return resource.toItemStack();
            }
        });

    private ItemStack getStack(final ItemResource itemResource) {
        return stackCache.getUnchecked(itemResource);
    }

    @Override
    public String formatAmount(final long amount, final boolean withUnits) {
        return !withUnits ? format(amount) : formatWithUnits(amount);
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
            Item.TooltipContext.EMPTY,
            minecraft.player,
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
    }

    @Override
    public void render(final ResourceKey resource, final GuiGraphicsExtractor graphics, final int x, final int y) {
        if (!(resource instanceof ItemResource itemResource)) {
            return;
        }
        final ItemStack stack = getStack(itemResource);
        graphics.item(stack, x, y);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    @Override
    public void render(final ResourceKey resource, final PoseStack poseStack, final SubmitNodeCollector nodes,
                       final int light, final long seed) {
        if (!(resource instanceof ItemResource itemResource)) {
            return;
        }
        final ItemStack stack = getStack(itemResource);
        final ItemStackRenderState renderState = new ItemStackRenderState();
        final Minecraft minecraft = Minecraft.getInstance();
        final ItemModelResolver resolver = minecraft.getItemModelResolver();
        final ClientLevel level = requireNonNull(minecraft.level);
        resolver.updateForTopItem(
            renderState,
            stack,
            ItemDisplayContext.GUI,
            level,
            new ItemOwner() {
                @Override
                public Level level() {
                    return level;
                }

                @Override
                public Vec3 position() {
                    return Vec3.ZERO;
                }

                @Override
                public float getVisualRotationYInDegrees() {
                    return 0;
                }
            },
            (int) seed
        );
        poseStack.mulPose(IN_WORLD_SCALE);
        poseStack.last().normal().rotateX(Mth.DEG_TO_RAD * -45f);
        renderState.submit(poseStack, nodes, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
