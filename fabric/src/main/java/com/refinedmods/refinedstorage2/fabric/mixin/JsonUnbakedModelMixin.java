package com.refinedmods.refinedstorage2.fabric.mixin;

import com.refinedmods.refinedstorage2.fabric.render.FullbrightHooks;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @link https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/fabric/master/src/main/java/appeng/mixins/unlitquad/JsonUnbakedModelMixin.java
 */
@Mixin(JsonUnbakedModel.class)
public class JsonUnbakedModelMixin {
    @Inject(method = "createQuad", at = @At("RETURN"), cancellable = true, require = 1, allow = 1)
    private static void onBakeFace(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings, Identifier id, CallbackInfoReturnable<BakedQuad> cri) {
        if (elementFace instanceof FullbrightHooks.FullbrightModelElementFace) {
            cri.setReturnValue(FullbrightHooks.makeFullbright(cri.getReturnValue()));
        }
    }
}
