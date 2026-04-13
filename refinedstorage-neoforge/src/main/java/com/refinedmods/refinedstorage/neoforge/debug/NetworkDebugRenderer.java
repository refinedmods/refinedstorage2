package com.refinedmods.refinedstorage.neoforge.debug;

import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Brightness;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class NetworkDebugRenderer {
    private static final int FULL_BRIGHT = Brightness.FULL_BRIGHT.pack();
    @SuppressWarnings("deprecation")
    private static final SpriteId BASE_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS,
        Identifier.parse("white_concrete"));
    private static final float MAX = 1.01F;
    private static final float MIN = -0.01F;

    private NetworkDebugRenderer() {
    }

    @SubscribeEvent
    public static void renderDebugOverlay(final AddSectionGeometryEvent e) {
        final TextureAtlasSprite dummySprite = Minecraft.getInstance().getAtlasManager().get(BASE_SPRITE);
        final BlockPos regionOrigin = e.getSectionOrigin().immutable();
        e.addRenderer(ctx -> {
            final VertexConsumer buf = ctx.getOrCreateChunkBuffer(ChunkSectionLayer.TRANSLUCENT);
            final PoseStack poseStack = new PoseStack();
            BlockPos.betweenClosed(regionOrigin, regionOrigin.offset(16, 16, 16))
                .forEach(pos -> renderAt(ctx.getRegion().getBlockEntity(pos), pos, regionOrigin, buf,
                    dummySprite.getU0(), dummySprite.getV1(), poseStack));
        });
    }

    private static void renderAt(@Nullable final BlockEntity blockEntity, final BlockPos pos,
                                 final BlockPos regionOrigin, final VertexConsumer buf, final float u, final float v,
                                 final PoseStack poseStack) {
        if (!(blockEntity instanceof AbstractBaseNetworkNodeContainerBlockEntity<?> container)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(
            (float) pos.getX() - regionOrigin.getX(),
            (float) pos.getY() - regionOrigin.getY(),
            (float) pos.getZ() - regionOrigin.getZ()
        );

        final int color = getColorFromId(container.getDebugNetworkId());
        final Matrix4f mat = poseStack.last().pose();

        buf.addVertex(mat, MIN, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);

        buf.addVertex(mat, MIN, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);

        buf.addVertex(mat, MAX, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);

        buf.addVertex(mat, MIN, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);

        buf.addVertex(mat, MAX, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MAX, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MAX, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MAX, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MAX)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);
        buf.addVertex(mat, MIN, MIN, MIN)
            .setUv(u, v)
            .setNormal(0, 0, 0)
            .setColor(color)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(FULL_BRIGHT);

        poseStack.popPose();
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
