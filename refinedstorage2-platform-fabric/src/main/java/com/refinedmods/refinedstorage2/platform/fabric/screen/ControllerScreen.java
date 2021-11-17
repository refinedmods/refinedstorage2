package com.refinedmods.refinedstorage2.platform.fabric.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.ControllerScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ControllerScreen extends BaseScreen<ControllerScreenHandler> {
    private static final ResourceLocation TEXTURE = Rs2Mod.createIdentifier("textures/gui/controller.png");

    private final ProgressWidget progressWidget;

    public ControllerScreen(ControllerScreenHandler screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 94;
        this.imageWidth = 176;
        this.imageHeight = 189;

        this.progressWidget = new ProgressWidget(80, 20, 16, 70, this::getPercentageFull, this::renderComponentTooltip, this::createTooltip);
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
    }

    private double getPercentageFull() {
        return (double) getMenu().getStored() / (double) getMenu().getCapacity();
    }

    private List<Component> createTooltip() {
        return Collections.singletonList(Rs2Mod.createTranslation(
                "misc",
                "stored_with_capacity",
                QuantityFormatter.format(getMenu().getStored()),
                QuantityFormatter.format(getMenu().getCapacity())
        ));
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        ScreenUtil.drawVersionInformation(matrices, font);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        progressWidget.render(matrices, mouseX - leftPos, mouseY - topPos, 0);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);
    }
}
