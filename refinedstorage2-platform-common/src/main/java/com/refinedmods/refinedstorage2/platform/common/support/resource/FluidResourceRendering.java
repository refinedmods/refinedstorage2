package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class FluidResourceRendering implements ResourceRendering {
    private static final DecimalFormat LESS_THAN_1_BUCKET_FORMATTER =
        new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat FORMATTER =
        new DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.US));

    @Override
    public String getDisplayedAmount(final long amount, final boolean withUnits) {
        if (!withUnits) {
            return format(amount);
        }
        return formatWithUnits(amount);
    }

    @Override
    public Component getDisplayName(final ResourceKey resource) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return Component.empty();
        }
        return Platform.INSTANCE.getFluidRenderer().getDisplayName(fluidResource);
    }

    @Override
    public List<Component> getTooltip(final ResourceKey resource) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return Collections.emptyList();
        }
        return Platform.INSTANCE.getFluidRenderer().getTooltip(fluidResource);
    }

    @Override
    public void render(final ResourceKey resource, final GuiGraphics graphics, final int x, final int y) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return;
        }
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, fluidResource);
    }

    @Override
    public void render(final ResourceKey resource,
                       final PoseStack poseStack,
                       final MultiBufferSource renderTypeBuffer,
                       final int light,
                       final Level level) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return;
        }
        Platform.INSTANCE.getFluidRenderer().render(poseStack, renderTypeBuffer, light, fluidResource);
    }

    public static String formatWithUnits(final long droplets) {
        final double buckets = convertToBuckets(droplets);
        if (buckets >= 1) {
            return AmountFormatting.formatWithUnits((long) Math.floor(buckets));
        } else {
            return LESS_THAN_1_BUCKET_FORMATTER.format(buckets);
        }
    }

    public static String format(final long droplets) {
        final double buckets = convertToBuckets(droplets);
        return FORMATTER.format(buckets);
    }

    private static double convertToBuckets(final long droplets) {
        return droplets / (double) Platform.INSTANCE.getBucketAmount();
    }
}
