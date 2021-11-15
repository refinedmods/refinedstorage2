package com.refinedmods.refinedstorage2.platform.fabric.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ExactModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ResourceFilterButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DiskDriveScreen extends BaseScreen<DiskDriveContainerMenu> {
    private static final ResourceLocation TEXTURE = Rs2Mod.createIdentifier("textures/gui/disk_drive.png");
    private static final TranslatableComponent DISKS_TEXT = Rs2Mod.createTranslation("gui", "disk_drive.disks");

    private final ProgressWidget progressWidget;
    private final Inventory playerInventory;

    public DiskDriveScreen(DiskDriveContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 129;
        this.imageWidth = 176;
        this.imageHeight = 223;
        this.playerInventory = inventory;

        this.progressWidget = new ProgressWidget(99, 54, 16, 70, menu::getProgress, this::renderComponentTooltip, this::createTooltip);
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new FilterModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new ExactModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new AccessModeSideButtonWidget(getMenu(), this::renderComponentTooltip));
        addSideButton(new PrioritySideButtonWidget(getMenu(), playerInventory, this, this::renderComponentTooltip));
        addRenderableWidget(new ResourceFilterButtonWidget(leftPos + imageWidth - ResourceFilterButtonWidget.WIDTH - 7, topPos + 4, menu));
    }

    private List<Component> createTooltip() {
        List<Component> tooltip = new ArrayList<>();
        long stored = getMenu().getStored();

        if (menu.hasInfiniteDisk()) {
            tooltip.add(Rs2Mod.createTranslation("misc", "stored", QuantityFormatter.format(stored)));
        } else {
            long capacity = getMenu().getCapacity();
            double progress = getMenu().getProgress();

            tooltip.add(Rs2Mod.createTranslation("misc", "stored_with_capacity", QuantityFormatter.format(stored), QuantityFormatter.format(capacity)));
            tooltip.add(Rs2Mod.createTranslation("misc", "full", (int) (progress * 100D)).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        ScreenUtil.drawVersionInformation(poseStack, font);

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
        font.draw(poseStack, DISKS_TEXT, 60, 42, 4210752);
        progressWidget.render(poseStack, mouseX - leftPos, mouseY - topPos, 0);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
