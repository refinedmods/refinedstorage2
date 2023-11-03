package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screens.Screen;

class ResourceFocusedStackProvider implements FocusedStackProvider {
    private final IngredientConverter converter;

    ResourceFocusedStackProvider(final IngredientConverter converter) {
        this.converter = converter;
    }

    @Override
    public CompoundEventResult<EntryStack<?>> provide(final Screen screen, final Point mouse) {
        if (!(screen instanceof AbstractBaseScreen<?> baseScreen)) {
            return CompoundEventResult.pass();
        }
        final ResourceTemplate<?> hoveredResource = baseScreen.getHoveredResource();
        if (hoveredResource == null) {
            return CompoundEventResult.pass();
        }
        final Object converted = converter.convertToIngredient(hoveredResource).orElse(null);
        if (converted instanceof EntryStack<?> stack) {
            return CompoundEventResult.interruptTrue(stack);
        }
        return CompoundEventResult.pass();
    }
}

