package com.refinedmods.refinedstorage.platform.fabric.mixin;

import com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage.platform.fabric.support.render.EmissiveModelRegistry;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.ModelBakerImpl.class)
public abstract class AbstractModelBakerImplMixin {
    @Inject(method = "bakeUncached", at = @At("RETURN"), cancellable = true)
    public void onBakeUncached(final UnbakedModel unbakedModel,
                               final ModelState modelState,
                               final CallbackInfoReturnable<BakedModel> cir) {
        if (!(unbakedModel instanceof BlockModel blockModel)) {
            return;
        }
        if (!blockModel.name.startsWith(IdentifierUtil.MOD_ID)) {
            return;
        }
        final BakedModel wrapped = EmissiveModelRegistry.INSTANCE.tryWrapAsEmissiveModel(
            ResourceLocation.parse(blockModel.name),
            cir.getReturnValue()
        );
        if (wrapped == null) {
            return;
        }
        cir.setReturnValue(wrapped);
    }
}
