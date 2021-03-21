package com.refinedmods.refinedstorage2.fabric.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public abstract class SideButtonWidget extends ButtonWidget implements ButtonWidget.TooltipSupplier {
    private static final Identifier TEXTURE = new Identifier(RefinedStorage2Mod.ID, "textures/icons.png");

    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;

    public SideButtonWidget(PressAction pressAction) {
        super(-1, -1, WIDTH, HEIGHT, LiteralText.EMPTY, pressAction);
    }

    protected abstract int getXTexture();

    protected abstract int getYTexture();

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getTextureManager().bindTexture(TEXTURE);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();

        this.hovered = mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;

        drawTexture(matrices, x, y, 238, hovered ? 35 : 16, WIDTH, HEIGHT);
        drawTexture(matrices, x + 1, y + 1, getXTexture(), getYTexture(), WIDTH, HEIGHT);

        if (hovered) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 0.5f);
            drawTexture(matrices, x, y, 238, 54, WIDTH, HEIGHT);
            RenderSystem.disableBlend();

            onTooltip(this, matrices, mouseX, mouseY);
        }
    }
}