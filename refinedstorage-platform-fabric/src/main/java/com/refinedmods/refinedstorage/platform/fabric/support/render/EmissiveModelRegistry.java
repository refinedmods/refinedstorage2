package com.refinedmods.refinedstorage.platform.fabric.support.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.resources.model.BakedModel;
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
    public BakedModel tryWrapAsEmissiveModel(final ResourceLocation model, final BakedModel bakedModel) {
        final Function<BakedModel, EmissiveBakedModel> wrapper = factories.get(model);
        if (wrapper != null) {
            LOGGER.debug("Made {} an emissive model", model);
            return wrapper.apply(bakedModel);
        }
        return null;
    }

    public void register(final ResourceLocation location,
                         final ResourceLocation... spriteLocations) {
        factories.put(location, bakedModel -> new EmissiveBakedModel(bakedModel, new HashSet<>(
            Arrays.asList(spriteLocations)
        )));
    }
}
