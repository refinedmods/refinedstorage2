package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor("bakedCache")
    Map<ModelBakery.BakedCacheKey, BakedModel> getBakedCache();
}
