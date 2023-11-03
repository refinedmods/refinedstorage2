package com.refinedmods.refinedstorage2.platform.fabric.support.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmissiveModelRegistry {
    public static final EmissiveModelRegistry INSTANCE = new EmissiveModelRegistry();

    private static final Logger LOGGER = LoggerFactory.getLogger(EmissiveModelRegistry.class);

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
