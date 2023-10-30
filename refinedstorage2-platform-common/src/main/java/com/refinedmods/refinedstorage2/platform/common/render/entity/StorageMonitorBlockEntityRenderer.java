package com.refinedmods.refinedstorage2.platform.common.render.entity;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.block.StorageMonitorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storagemonitor.StorageMonitorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class StorageMonitorBlockEntityRenderer implements BlockEntityRenderer<StorageMonitorBlockEntity> {
    private static final Quaternionf ROTATE_TO_FRONT = new Quaternionf().rotationY(Mth.DEG_TO_RAD * 180);
    private static final float FONT_SPACING = -0.23f;

    @Override
    public void render(final StorageMonitorBlockEntity blockEntity,
                       final float tickDelta,
                       final PoseStack poseStack,
                       final MultiBufferSource vertexConsumers,
                       final int light,
                       final int overlay) {
        final Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        final BiDirection direction = getDirection(blockEntity, level);
        if (direction == null) {
            return;
        }
        if (!blockEntity.isCurrentlyActive()) {
            return;
        }
        final ResourceAmountTemplate<?> template = blockEntity.getFilteredResource();
        if (template == null) {
            return;
        }
        doRender(
            blockEntity.getLevel(),
            poseStack,
            vertexConsumers,
            direction,
            template,
            blockEntity.getCurrentAmount()
        );
    }

    private <T> void doRender(final Level level,
                              final PoseStack poseStack,
                              final MultiBufferSource vertexConsumers,
                              final BiDirection direction,
                              final ResourceAmountTemplate<T> template,
                              final long amount) {
        final ResourceRendering<T> resourceRendering = PlatformApi.INSTANCE.getResourceRendering(
            template.getResource()
        );
        doRender(
            poseStack,
            vertexConsumers,
            direction.getQuaternion(),
            resourceRendering.getDisplayedAmount(amount, false),
            level,
            resourceRendering,
            template.getResource()
        );
    }

    private <T> void doRender(final PoseStack poseStack,
                              final MultiBufferSource renderTypeBuffer,
                              final Quaternionf rotation,
                              final String amount,
                              final Level level,
                              final ResourceRendering<T> resourceRendering,
                              final T resource) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(rotation);
        poseStack.mulPose(ROTATE_TO_FRONT);
        poseStack.translate(0, 0.05, 0.5);

        poseStack.pushPose();
        renderAmount(poseStack, renderTypeBuffer, amount);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, 0.01f);
        resourceRendering.render(resource, poseStack, renderTypeBuffer, LightTexture.FULL_BRIGHT, level);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderAmount(final PoseStack poseStack,
                              final MultiBufferSource renderTypeBuffer,
                              final String amount) {
        final Font font = Minecraft.getInstance().font;
        final float width = font.width(amount);
        poseStack.translate(0.0f, FONT_SPACING, 0.02f);
        poseStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        poseStack.scale(0.5f, 0.5f, 0);
        poseStack.translate(-0.5f * width, 0.0f, 0.5f);
        font.drawInBatch(
            amount,
            0,
            0,
            0xFFFFFF,
            false,
            poseStack.last().pose(),
            renderTypeBuffer,
            Font.DisplayMode.NORMAL,
            0,
            LightTexture.FULL_BRIGHT
        );
    }

    @Nullable
    private BiDirection getDirection(final StorageMonitorBlockEntity blockEntity, final Level level) {
        final BlockState state = level.getBlockState(blockEntity.getBlockPos());
        if (state.getBlock() instanceof StorageMonitorBlock) {
            return state.getValue(BiDirectionType.INSTANCE.getProperty());
        }
        return null;
    }
}
