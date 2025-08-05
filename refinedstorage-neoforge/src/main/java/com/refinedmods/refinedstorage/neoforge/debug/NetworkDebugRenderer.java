package com.refinedmods.refinedstorage.neoforge.debug;

import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;

public final class NetworkDebugRenderer {
    private NetworkDebugRenderer() {
    }

    @SubscribeEvent
    public static void renderDebugOverlay(final AddSectionGeometryEvent e) {
        final BlockPos regionOrigin = e.getSectionOrigin().immutable();
        e.addRenderer(ctx -> {
            final var buf = ctx.getOrCreateChunkBuffer(RenderType.translucent());
            BlockPos.betweenClosed(regionOrigin, regionOrigin.offset(16, 16, 16))
                .forEach(pos -> renderAt(ctx, pos, regionOrigin, buf));
        });
    }

    private static void renderAt(final AddSectionGeometryEvent.SectionRenderingContext ctx, final BlockPos pos,
                                 final BlockPos regionOrigin, final VertexConsumer buf) {
        final BlockEntity blockEntity = ctx.getRegion().getBlockEntity(pos);
        if (!(blockEntity instanceof AbstractBaseNetworkNodeContainerBlockEntity<?> container)) {
            return;
        }

        ctx.getPoseStack().pushPose();
        ctx.getPoseStack().translate((float) pos.getX() - regionOrigin.getX(),
            (float) pos.getY() - regionOrigin.getY(), (float) pos.getZ() - regionOrigin.getZ());

        final int color = getColorFromId(container.getDebugNetworkId());
        final var mat = ctx.getPoseStack().last().pose();

        final float max = 1.01F;
        final float min = -0.01F;

        buf.addVertex(mat, min, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, min, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, max, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, min, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);

        buf.addVertex(mat, max, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, max, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, max, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, max, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, max)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);
        buf.addVertex(mat, min, min, min)
            .setUv(-100, -100)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT);

        ctx.getPoseStack().popPose();
    }

    private static int getColorFromId(final int id) {
        if (id == -1) {
            return 0xFF000000;
        }
        final byte[] hash = getIdHash(id);
        final int r = Byte.toUnsignedInt(hash[0]);
        final int g = Byte.toUnsignedInt(hash[1]);
        final int b = Byte.toUnsignedInt(hash[2]);
        final int a = 128;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static byte[] getIdHash(final int id) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] input = ByteBuffer.allocate(4).putInt(id).array();
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
