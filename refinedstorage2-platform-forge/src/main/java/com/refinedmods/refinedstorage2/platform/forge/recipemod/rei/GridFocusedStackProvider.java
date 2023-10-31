package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.grid.screen.AbstractGridScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

public class GridFocusedStackProvider implements FocusedStackProvider {
    private final IngredientConverter converter;

    public GridFocusedStackProvider(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (!(screen instanceof AbstractGridScreen<?> gridScreen)) {
            return CompoundEventResult.pass();
        }
        final GridResource resource = gridScreen.getCurrentGridResource();
        if (resource == null) {
            return CompoundEventResult.pass();
        }
        final Object converted = converter.convertToIngredient(resource).orElse(null);
        if (converted instanceof EntryStack<?> stack) {
            return CompoundEventResult.interruptTrue(stack);
        }
        return CompoundEventResult.pass();
    }
}
