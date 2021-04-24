package com.refinedmods.refinedstorage2.fabric.mixin;

import com.refinedmods.refinedstorage2.fabric.render.FullbrightHooks;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @link https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/fabric/master/src/main/java/appeng/mixins/unlitquad/ModelLoaderMixin.java
 */
@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Inject(method = "loadModelFromJson", at = @At("HEAD"), allow = 1)
    protected void onBeginLoadModel(Identifier location, CallbackInfoReturnable<JsonUnbakedModel> cri) {
        FullbrightHooks.beginDeserializingModel(location);
    }

    @Inject(method = "loadModelFromJson", at = @At("RETURN"))
    protected void onEndLoadModel(Identifier location, CallbackInfoReturnable<JsonUnbakedModel> cri) {
        FullbrightHooks.endDeserializingModel();
    }
}
