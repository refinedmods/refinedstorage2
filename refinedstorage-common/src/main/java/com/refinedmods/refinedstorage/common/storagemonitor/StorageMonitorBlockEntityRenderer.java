package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.content.Blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Brightness;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class StorageMonitorBlockEntityRenderer
    implements BlockEntityRenderer<StorageMonitorBlockEntity, StorageMonitorBlockEntityRenderState> {
    private static final Quaternionf ROTATE_TO_FRONT = new Quaternionf().rotationY(Mth.DEG_TO_RAD * 180);
    private static final float FONT_SPACING = -0.23f;
    private static final int FULL_BRIGHT = Brightness.FULL_BRIGHT.pack();

    @Override
    public StorageMonitorBlockEntityRenderState createRenderState() {
        return new StorageMonitorBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(final StorageMonitorBlockEntity blockEntity,
                                   final StorageMonitorBlockEntityRenderState state,
                                   final float partialTicks, final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.configuredResource = blockEntity.getConfiguredResource();
        state.direction = Blocks.INSTANCE.getStorageMonitor().getDirection(blockEntity.getBlockState());
        state.active = blockEntity.isCurrentlyActive();
        state.amount = blockEntity.getCurrentAmount();
        state.seed = blockEntity.getBlockPos().asLong();
    }

    @Override
    public void submit(final StorageMonitorBlockEntityRenderState state, final PoseStack poseStack,
                       final SubmitNodeCollector nodes,
                       final CameraRenderState cameraRenderState) {
        final ResourceKey resource = state.configuredResource;
        if (!state.active || resource == null || state.direction == null) {
            return;
        }
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(state.direction.getQuaternion());
        poseStack.mulPose(ROTATE_TO_FRONT);
        poseStack.translate(0, 0, 0.5);
        renderAmount(poseStack, nodes, rendering.formatAmount(state.amount));
        renderResource(poseStack, nodes, resource, rendering, state.seed);
        poseStack.popPose();
    }

    private void renderAmount(final PoseStack poseStack, final SubmitNodeCollector nodes,
                              final String amount) {
        final Font font = Minecraft.getInstance().font;
        final float width = font.width(amount);
        poseStack.pushPose();
        poseStack.translate(0.0f, FONT_SPACING, 0.02f);
        poseStack.scale(1.0f / 62.0f, -1.0f / 62.0f, 1.0f / 62.0f);
        poseStack.scale(0.5f, 0.5f, 0);
        poseStack.translate(-0.5f * width, 0.0f, 0.5f);
        nodes.submitText(poseStack, 0, 0, Component.literal(amount).getVisualOrderText(),
            false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF, 0, 0);
        poseStack.popPose();
    }

    private void renderResource(final PoseStack poseStack, final SubmitNodeCollector nodes,
                                final ResourceKey resource, final ResourceRendering rendering,
                                final long seed) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.01f);
        rendering.render(resource, poseStack, nodes, FULL_BRIGHT, seed);
        poseStack.popPose();
    }
}
