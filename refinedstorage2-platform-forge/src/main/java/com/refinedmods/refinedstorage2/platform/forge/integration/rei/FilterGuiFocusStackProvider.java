package com.refinedmods.refinedstorage2.platform.forge.integration.rei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

public class FilterGuiFocusStackProvider implements FocusedStackProvider {
    private final IngredientConverter converter;

    public FilterGuiFocusStackProvider(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (screen instanceof AbstractBaseScreen<?> containerScreen
            && containerScreen.getMenu() instanceof AbstractResourceFilterContainerMenu
            && containerScreen.getHoveredSlot() instanceof ResourceFilterSlot slot
        ) {
            final FilteredResource filteredResource = slot.getFilteredResource();
            if (filteredResource == null) {
                return CompoundEventResult.pass();
            }
            final Object converted = converter.convertToIngredient(filteredResource).orElse(null);
            if (converted instanceof EntryStack<?> stack) {
                return CompoundEventResult.interruptTrue(stack);
            }
        }
        return CompoundEventResult.pass();
    }
}

