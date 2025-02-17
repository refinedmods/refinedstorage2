package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

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
    private static final DecimalFormat FORMATTER = new DecimalFormat(
        "#,###.###",
        DecimalFormatSymbols.getInstance(Locale.US)
    );

    private final long bucketAmount;

    public FluidResourceRendering(final long bucketAmount) {
        this.bucketAmount = bucketAmount;
    }

    @Override
    public String formatAmount(final long amount, final boolean withUnits) {
        if (!withUnits) {
            return format(amount, bucketAmount);
        }
        return formatWithUnits(amount, bucketAmount);
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

    private static String formatWithUnits(final long droplets, final long bucketAmount) {
        final double buckets = convertToBuckets(droplets, bucketAmount);
        return IdentifierUtil.formatWithUnits(buckets) + "b";
    }

    private static String format(final long droplets, final long bucketAmount) {
        final double buckets = convertToBuckets(droplets, bucketAmount);
        return FORMATTER.format(buckets);
    }

    private static double convertToBuckets(final long droplets, final long bucketAmount) {
        return droplets / (double) bucketAmount;
    }
}
