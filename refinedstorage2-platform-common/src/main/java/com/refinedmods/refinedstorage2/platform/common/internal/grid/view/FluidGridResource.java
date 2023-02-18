package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

public class FluidGridResource extends AbstractGridResource<FluidResource> {
    private final FluidResource fluidResource;
    private final int id;

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // forge deprecates Registry access
    public FluidGridResource(final ResourceAmount<FluidResource> resourceAmount,
                             final String name,
                             final String modId,
                             final String modName,
                             final Set<String> tags,
                             final String tooltip) {
        super(resourceAmount, name, Map.of(
            GridResourceAttributeKeys.MOD_ID, Set.of(modId),
            GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            GridResourceAttributeKeys.TAGS, tags,
            GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = BuiltInRegistries.FLUID.getId(resourceAmount.getResource().fluid());
        this.fluidResource = resourceAmount.getResource();
    }

    @Override
    public int getRegistryId() {
        return id;
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(
            StorageChannelTypes.FLUID,
            fluidResource,
            extractMode,
            cursor
        );
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        // no-op
    }

    @Override
    public void render(final PoseStack poseStack, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(
            poseStack,
            x,
            y,
            0,
            fluidResource
        );
    }

    @Override
    public String getDisplayedAmount() {
        return Platform.INSTANCE.getBucketAmountFormatter().formatWithUnits(getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return Platform.INSTANCE.getBucketAmountFormatter().format(getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(fluidResource);
    }
}
