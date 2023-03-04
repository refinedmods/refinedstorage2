package com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

public class FilteredResourceFocusedStackProvider implements FocusedStackProvider {
    private final IngredientConverter converter;

    public FilteredResourceFocusedStackProvider(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (!(screen instanceof AbstractBaseScreen<?> baseScreen)) {
            return CompoundEventResult.pass();
        }
        final FilteredResource<?> filteredResource = baseScreen.getFilteredResource();
        if (filteredResource == null) {
            return CompoundEventResult.pass();
        }
        final Object converted = converter.convertToIngredient(filteredResource).orElse(null);
        if (converted instanceof EntryStack<?> stack) {
            return CompoundEventResult.interruptTrue(stack);
        }
        return CompoundEventResult.pass();
    }
}

