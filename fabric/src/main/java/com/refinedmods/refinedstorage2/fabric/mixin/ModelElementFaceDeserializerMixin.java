package com.refinedmods.refinedstorage2.fabric.mixin;

import com.refinedmods.refinedstorage2.fabric.render.FullbrightHooks;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @link https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/fabric/master/src/main/java/appeng/mixins/unlitquad/ModelElementFaceDeserializerMixin.java
 */
@Mixin(ModelElementFace.Deserializer.class)
public class ModelElementFaceDeserializerMixin {
    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true, allow = 1, remap = false)
    public void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<ModelElementFace> cri) {
        if (!FullbrightHooks.isFullbrightExtensionEnabled()) {
            return;
        }
        ModelElementFace modelElement = cri.getReturnValue();
        cri.setReturnValue(FullbrightHooks.enhanceModelElementFace(modelElement, jsonElement));
    }
}
