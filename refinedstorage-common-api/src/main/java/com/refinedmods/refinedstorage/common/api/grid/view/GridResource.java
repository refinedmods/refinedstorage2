package com.refinedmods.refinedstorage.common.api.grid.view;

import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface GridResource {
    @Nullable
    TrackedResource getTrackedResource(Function<ResourceKey, @Nullable TrackedResource> trackedResourceProvider);

    long getAmount(ResourceRepository<GridResource> repository);

    List<String> getSearchableNames();

    String getHoverName();

    Set<String> getAttribute(GridResourceAttributeKey key);

    boolean isAutocraftable(ResourceRepository<GridResource> repository);

    boolean canExtract(ItemStack carriedStack, ResourceRepository<GridResource> repository);

    void onExtract(GridExtractMode extractMode,
                   boolean cursor,
                   GridExtractionStrategy extractionStrategy);

    void onScroll(GridScrollMode scrollMode,
                  GridScrollingStrategy scrollingStrategy);

    void render(GuiGraphicsExtractor graphics, int x, int y);

    String getDisplayedAmount(ResourceRepository<GridResource> repository);

    String getAmountInTooltip(ResourceRepository<GridResource> repository);

    boolean belongsToResourceType(ResourceType resourceType);

    List<Component> getTooltip();

    Optional<TooltipComponent> getTooltipImage();

    int getRegistryId();

    List<ClientTooltipComponent> getExtractionHints(ItemStack carriedStack,
                                                    ResourceRepository<GridResource> repository);

    @Nullable
    ResourceAmount getAutocraftingRequest();

    @Nullable
    @API(status = API.Status.INTERNAL)
    PlatformResourceKey getResourceForRecipeMods();
}
