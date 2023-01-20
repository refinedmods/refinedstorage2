package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.EmissiveModelRegistry;

import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.ModelBakerImpl.class)
public abstract class AbstractModelBakerImplMixin {
    @Shadow(remap = false)
    private ModelBakery field_40571;

    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    public void onBake(final ResourceLocation resourceLocation,
                       final ModelState modelState,
                       final CallbackInfoReturnable<BakedModel> returnable) {
        if (!resourceLocation.getNamespace().equals(IdentifierUtil.MOD_ID)) {
            return;
        }
        final BakedModel emissive = EmissiveModelRegistry.INSTANCE.makeEmissive(
            resourceLocation,
            returnable.getReturnValue()
        );
        if (emissive == null) {
            return;
        }
        final ModelBakery.BakedCacheKey cacheKey = new ModelBakery.BakedCacheKey(
            resourceLocation,
            modelState.getRotation(),
            modelState.isUvLocked()
        );
        final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = ((ModelBakeryAccessor) field_40571)
            .getBakedCache();
        bakedCache.put(cacheKey, emissive);
        returnable.setReturnValue(emissive);
    }
}
