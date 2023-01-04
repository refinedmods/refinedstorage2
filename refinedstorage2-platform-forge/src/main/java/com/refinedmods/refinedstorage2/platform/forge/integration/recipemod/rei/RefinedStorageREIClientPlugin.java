package com.refinedmods.refinedstorage2.platform.forge.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class RefinedStorageREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(final ScreenRegistry registry) {
        final IngredientConverter converter = PlatformApi.INSTANCE.getIngredientConverter();
        registry.registerFocusedStack(new GridFocusedStackProvider(converter));
        registry.registerFocusedStack(new FilteredResourceFocusedStackProvider(converter));
    }

    public static void registerIngredientConverters() {
        PlatformApi.INSTANCE.registerIngredientConverter(new GridResourceIngredientConverter());
        PlatformApi.INSTANCE.registerIngredientConverter(new FilteredResourceIngredientConverter());
    }
}
