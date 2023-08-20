package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.MouseWithIconClientTooltipComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

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
    public List<? extends ClientTooltipComponent> getExtractionHints() {
        return Platform.INSTANCE.convertToBucket(fluidResource).map(
            bucket -> new MouseWithIconClientTooltipComponent(
                MouseWithIconClientTooltipComponent.Type.LEFT,
                (graphics, x, y) -> graphics.renderItem(bucket, x, y),
                null
            )
        ).stream().toList();
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
    public void render(final GuiGraphics graphics, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, fluidResource);
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

    @Override
    public Optional<TooltipComponent> getTooltipImage() {
        return Optional.empty();
    }
}
