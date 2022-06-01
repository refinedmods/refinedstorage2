package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageAccessor;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ExactModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ResourceFilterButtonWidget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class StorageScreen<T extends AbstractContainerMenu & StorageAccessor> extends BaseScreen<T> {
    private final ProgressWidget progressWidget;
    private final Inventory playerInventory;

    protected StorageScreen(T menu, Inventory inventory, Component title, int progressWidgetX) {
        super(menu, inventory, title);

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 129;
        this.imageWidth = 176;
        this.imageHeight = 223;
        this.playerInventory = inventory;

        this.progressWidget = new ProgressWidget(progressWidgetX, 54, 16, 70, menu::getProgress, this::renderComponentTooltip, this::createTooltip);
        addRenderableWidget(progressWidget);
    }

    protected abstract boolean isResourceFilterButtonActive();

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new FilterModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new ExactModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new AccessModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new PrioritySideButtonWidget(getMenu(), playerInventory, this, this::renderComponentTooltip));
        ResourceFilterButtonWidget resourceFilterButton = new ResourceFilterButtonWidget(leftPos + imageWidth - ResourceFilterButtonWidget.WIDTH - 7, topPos + 4, menu);
        resourceFilterButton.active = isResourceFilterButtonActive();
        addRenderableWidget(resourceFilterButton);
    }

    private List<Component> createTooltip() {
        List<Component> tooltip = new ArrayList<>();
        long stored = getMenu().getStored();

        if (!menu.hasCapacity()) {
            tooltip.add(createTranslation("misc", "stored", format(stored)));
        } else {
            long capacity = getMenu().getCapacity();
            double progress = getMenu().getProgress();

            tooltip.add(createTranslation("misc", "stored_with_capacity", format(stored), format(capacity)));
            tooltip.add(createTranslation("misc", "full", (int) (progress * 100D)).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    protected String format(long qty) {
        return QuantityFormatter.format(qty);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        super.renderBg(poseStack, delta, mouseX, mouseY);
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
