package com.refinedmods.refinedstorage2.platform.common.integration.jei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei.FilterIngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei.GridIngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.AbstractGridScreen;

import javax.annotation.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

@JeiPlugin
public class RefinedStorageModPlugin implements IModPlugin {
    private static final ResourceLocation ID = createIdentifier("plugin");
    private static final Logger LOGGER = LoggerFactory.getLogger(RefinedStorageModPlugin.class);
    @Nullable
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(final IJeiRuntime newRuntime) {
        if (runtime == null) {
            this.initializePlatform(newRuntime.getJeiHelpers().getPlatformFluidHelper());
        }
        RefinedStorageModPlugin.runtime = newRuntime;
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        final IngredientConverter converter = PlatformApi.INSTANCE.getIngredientConverter();
        registration.addGenericGuiContainerHandler(AbstractBaseScreen.class, new FilterGuiContainerHandler(converter));
        registration.addGenericGuiContainerHandler(AbstractGridScreen.class, new GridGuiContainerHandler(converter));
    }

    @Nullable
    public static IJeiRuntime getRuntime() {
        return runtime;
    }

    private void initializePlatform(final IPlatformFluidHelper<?> fluidHelper) {
        LOGGER.info("Activating JEI integration");
        PlatformApi.INSTANCE.registerIngredientConverter(new GridIngredientConverter(fluidHelper));
        PlatformApi.INSTANCE.registerIngredientConverter(new FilterIngredientConverter(fluidHelper));
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
}
