package com.refinedmods.refinedstorage2.platform.forge.integration.rei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei.FilterIngredientConverter;
import com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei.GridIngredientConverter;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPlugin;

@REIPlugin
public class RefinedStorageModPlugin implements REIClientPlugin {
    private boolean convertersRegistered = false;

    @Override
    public void registerScreens(final ScreenRegistry registry) {
        if (!convertersRegistered) {
            registerIngredientConverters();
        }
        final IngredientConverter converter = PlatformApi.INSTANCE.getIngredientConverter();
        registry.registerFocusedStack(new GridGuiFocusStackProvider(converter));
        registry.registerFocusedStack(new FilterGuiFocusStackProvider(converter));
    }

    private void registerIngredientConverters() {
        this.convertersRegistered = true;
        PlatformApi.INSTANCE.registerIngredientConverter(new GridIngredientConverter());
        PlatformApi.INSTANCE.registerIngredientConverter(new FilterIngredientConverter());
    }
}
