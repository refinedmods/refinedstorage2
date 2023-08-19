package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.AbstractGridScreen;

import javax.annotation.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

@JeiPlugin
public class RefinedStorageJeiModPlugin implements IModPlugin {
    private static final ResourceLocation ID = createIdentifier("plugin");
    private static final Logger LOGGER = LoggerFactory.getLogger(RefinedStorageJeiModPlugin.class);
    @Nullable
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipeTransferHandlers(final IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new CraftingGridRecipeTransferHandler(), RecipeTypes.CRAFTING);
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime newRuntime) {
        if (runtime == null) {
            initializePlatform(newRuntime.getJeiHelpers().getPlatformFluidHelper());
        }
        RefinedStorageJeiModPlugin.runtime = newRuntime;
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        final IngredientConverter converter = PlatformApi.INSTANCE.getIngredientConverter();
        registration.addGenericGuiContainerHandler(
            AbstractBaseScreen.class,
            new ResourceGuiContainerHandler(converter, registration.getJeiHelpers().getIngredientManager())
        );
        registration.addGenericGuiContainerHandler(
            AbstractGridScreen.class,
            new GridGuiContainerHandler(converter, registration.getJeiHelpers().getIngredientManager())
        );
        registration.addGenericGuiContainerHandler(
            AbstractBaseScreen.class,
            new ExclusionZonesGuiContainerHandler()
        );
        registration.addGhostIngredientHandler(AbstractBaseScreen.class, new GhostIngredientHandler(converter));
    }

    @Nullable
    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    private void initializePlatform(final IPlatformFluidHelper<?> fluidHelper) {
        LOGGER.info("Enabling JEI integration");
        registerIngredientConverters(fluidHelper);
        registerGridSynchronizers();
    }

    private void registerGridSynchronizers() {
        final JeiProxy jeiProxy = new JeiProxy();
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("jei"),
            new JeiGridSynchronizer(jeiProxy, false)
        );
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("jei_two_way"),
            new JeiGridSynchronizer(jeiProxy, true)
        );
    }

    private void registerIngredientConverters(final IPlatformFluidHelper<?> fluidHelper) {
        PlatformApi.INSTANCE.registerIngredientConverter(new GridResourceIngredientConverter(fluidHelper));
        PlatformApi.INSTANCE.registerIngredientConverter(new ResourceIngredientConverter(fluidHelper));
    }
}
