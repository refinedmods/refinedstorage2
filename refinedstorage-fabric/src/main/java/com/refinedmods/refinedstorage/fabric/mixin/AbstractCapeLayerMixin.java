package com.refinedmods.refinedstorage.fabric.mixin;

import com.refinedmods.refinedstorage.common.cape.TenthAnniversaryCapeRenderer;
import com.refinedmods.refinedstorage.fabric.cape.TenthAnniversaryCape;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class AbstractCapeLayerMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private PlayerModel<AbstractClientPlayer> parentModel() {
        return (PlayerModel<AbstractClientPlayer>) ((RenderLayer<?, ?>) (Object) this).getParentModel();
    }

    @Inject(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/MultiBufferSource;"
            + "ILnet/minecraft/client/player/AbstractClientPlayer;"
            + "FFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderCape(
        final PoseStack poseStack,
        final MultiBufferSource buffer,
        final int packedLight,
        final AbstractClientPlayer livingEntity,
        final float limbSwing,
        final float limbSwingAmount,
        final float partialTicks,
        final float ageInTicks,
        final float netHeadYaw,
        final float headPitch,
        final CallbackInfo ci
    ) {
        if (livingEntity.isInvisible()) {
            return;
        }
        final ItemStack chestStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.is(Items.ELYTRA)) {
            return;
        }
        if (Boolean.TRUE.equals(livingEntity.getAttached(TenthAnniversaryCape.ATTACHMENT))) {
            ci.cancel();
            TenthAnniversaryCapeRenderer.render(poseStack, buffer, packedLight, livingEntity, partialTicks,
                parentModel());
        }
    }
}
