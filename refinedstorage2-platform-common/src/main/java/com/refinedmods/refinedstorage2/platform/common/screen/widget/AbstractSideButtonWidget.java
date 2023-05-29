package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public abstract class AbstractSideButtonWidget extends Button {
    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;

    protected AbstractSideButtonWidget(final OnPress pressAction) {
        super(-1, -1, WIDTH, HEIGHT, Component.empty(), pressAction, DEFAULT_NARRATION);
    }

    protected abstract int getXTexture();

    protected abstract int getYTexture();

    protected ResourceLocation getTextureIdentifier() {
        return TextureIds.ICONS;
    }

    @Override
    public void renderWidget(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getTextureIdentifier());
        RenderSystem.enableDepthTest();

        blit(poseStack, getX(), getY(), 238, isHovered ? 35 : 16, WIDTH, HEIGHT);
        blit(poseStack, getX() + 1, getY() + 1, getXTexture(), getYTexture(), WIDTH, HEIGHT);

        if (isHovered) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
            blit(poseStack, getX(), getY(), 238, 54, WIDTH, HEIGHT);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        if (isHovered) {
            onTooltip(poseStack, mouseX, mouseY); // TODO - remove use setTooltip.
        }

        RenderSystem.disableDepthTest();
    }

    protected abstract void onTooltip(PoseStack poseStack, int mouseX, int mouseY);
}
