package com.refinedmods.refinedstorage2.platform.fabric.render.model;

import com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.EmissiveBakedModel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EmissiveModelRegistry {
    public static final EmissiveModelRegistry INSTANCE = new EmissiveModelRegistry();

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceLocation, Function<BakedModel, EmissiveBakedModel>> factories = new HashMap<>();

    private EmissiveModelRegistry() {
    }

    @Nullable
    public BakedModel makeEmissive(final ResourceLocation model, final BakedModel bakedModel) {
        if (model instanceof ModelResourceLocation modelResourceLocation) {
            return makeEmissive(
                new ResourceLocation(modelResourceLocation.getNamespace(), modelResourceLocation.getPath()),
                bakedModel
            );
        }
        final Function<BakedModel, EmissiveBakedModel> wrapper = factories.get(model);
        if (wrapper != null) {
            LOGGER.debug("Made {} an emissive model", model);
            return wrapper.apply(bakedModel);
        }
        return null;
    }

    public void register(final ResourceLocation location,
                         final ResourceLocation spriteLocation) {
        factories.put(location, bakedModel -> new EmissiveBakedModel(bakedModel, spriteLocation));
    }
}
