package com.refinedmods.refinedstorage2.fabric.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.ControllerScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ControllerScreen extends BaseScreen<ControllerScreenHandler> {
    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/controller.png");

    private final ProgressWidget progressWidget;

    public ControllerScreen(ControllerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 94;
        this.backgroundWidth = 176;
        this.backgroundHeight = 189;

        this.progressWidget = new ProgressWidget(80, 20, 16, 70, this::getPercentageFull, this::renderTooltip, this::createTooltip);
        addDrawableChild(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
    }

    private double getPercentageFull() {
        return (double) getScreenHandler().getStored() / (double) getScreenHandler().getCapacity();
    }

    private List<Text> createTooltip() {
        return Collections.singletonList(Rs2Mod.createTranslation(
                "misc",
                "stored_with_capacity",
                QuantityFormatter.format(getScreenHandler().getStored()),
                QuantityFormatter.format(getScreenHandler().getCapacity())
        ));
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        ScreenUtil.drawVersionInformation(matrices, textRenderer);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        progressWidget.render(matrices, mouseX - x, mouseY - y, 0);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
