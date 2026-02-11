package com.refinedmods.refinedstorage.common.cape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class TenthAnniversaryCapeRenderer {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/tenth_anniversary_cape.png");

    private TenthAnniversaryCapeRenderer() {
    }

    public static void render(final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight,
                              final AbstractClientPlayer player, final float partialTicks,
                              final PlayerModel<AbstractClientPlayer> playerModel) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        final double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak)
            - Mth.lerp(partialTicks, player.xo, player.getX());
        final double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak)
            - Mth.lerp(partialTicks, player.yo, player.getY());
        final double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak)
            - Mth.lerp(partialTicks, player.zo, player.getZ());
        final float f = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        final double d3 = Mth.sin(f * ((float) Math.PI / 180F));
        final double d4 = (-Mth.cos(f * ((float) Math.PI / 180F)));
        float f1 = (float) d1 * 10.0F;
        f1 = Mth.clamp(f1, -6.0F, 32.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        f2 = Mth.clamp(f2, 0.0F, 150.0F);
        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
        f3 = Mth.clamp(f3, -20.0F, 20.0F);
        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        final float f4 = Mth.lerp(partialTicks, player.oBob, player.bob);
        f1 += Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * f4;
        if (player.isCrouching()) {
            f1 += 25.0F;
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
        poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
        final VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        playerModel.renderCloak(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
