package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.EmissiveModelRegistry;

import java.util.Map;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @Shadow
    private Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache;

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
        final Triple<ResourceLocation, Transformation, Boolean> triple = Triple.of(
            resourceLocation,
            modelState.getRotation(),
            modelState.isUvLocked()
        );
        bakedCache.put(triple, emissive);
        returnable.setReturnValue(emissive);
    }
}
