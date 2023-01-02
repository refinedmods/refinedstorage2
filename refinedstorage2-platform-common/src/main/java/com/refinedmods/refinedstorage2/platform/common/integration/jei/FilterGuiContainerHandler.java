package com.refinedmods.refinedstorage2.platform.common.integration.jei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import javax.annotation.Nullable;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class FilterGuiContainerHandler
    implements IGuiContainerHandler<AbstractContainerScreen<?>> {
    private final IngredientConverter converter;

    public FilterGuiContainerHandler(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    @Nullable
    public Object getIngredientUnderMouse(
        final AbstractContainerScreen<?> screen,
        final double mouseX,
        final double mouseY
    ) {
        if (screen instanceof AbstractBaseScreen<?> containerScreen) {
            if (containerScreen.getHoveredSlot() instanceof ResourceFilterSlot slot) {
                final FilteredResource filteredResource = slot.getFilteredResource();
                return handleFilteredResource(filteredResource);
            }
        }
        return null;
    }

    @Nullable
    public Object handleFilteredResource(@Nullable final FilteredResource filteredResource) {
        if (filteredResource == null) {
            return null;
        }
        return converter.convertToIngredient(filteredResource).orElse(null);
    }
}
