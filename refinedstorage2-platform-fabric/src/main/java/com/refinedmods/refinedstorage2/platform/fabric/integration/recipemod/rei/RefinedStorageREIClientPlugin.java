package com.refinedmods.refinedstorage2.platform.fabric.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
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
