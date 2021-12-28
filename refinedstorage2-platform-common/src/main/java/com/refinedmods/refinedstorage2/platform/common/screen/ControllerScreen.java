package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;

import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerScreen extends BaseScreen<ControllerContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/controller.png");

    private final ProgressWidget progressWidget;

    public ControllerScreen(ControllerContainerMenu menu, Inventory playerInventory, Component text) {
        super(menu, playerInventory, text);

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
        return Collections.singletonList(createTranslation(
                "misc",
                "stored_with_capacity",
                QuantityFormatter.format(getMenu().getStored()),
                QuantityFormatter.format(getMenu().getCapacity())
        ));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        progressWidget.render(poseStack, mouseX - leftPos, mouseY - topPos, 0);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
