package com.refinedmods.refinedstorage2.platform.fabric.screen;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ExactModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class DiskDriveScreen extends BaseScreen<DiskDriveScreenHandler> {
    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/disk_drive.png");
    private static final TranslatableText DISKS_TEXT = Rs2Mod.createTranslation("gui", "disk_drive.disks");

    private final ProgressWidget progressWidget;
    private final PlayerInventory playerInventory;

    public DiskDriveScreen(DiskDriveScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 129;
        this.backgroundWidth = 176;
        this.backgroundHeight = 223;
        this.playerInventory = inventory;

        this.progressWidget = new ProgressWidget(99, 54, 16, 70, handler::getProgress, this::renderTooltip, this::createTooltip);
        addDrawableChild(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new FilterModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new ExactModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new AccessModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new PrioritySideButtonWidget(getScreenHandler(), playerInventory, this, this::renderTooltip));
    }

    private List<Text> createTooltip() {
        List<Text> tooltip = new ArrayList<>();
        long stored = getScreenHandler().getStored();

        if (handler.hasInfiniteDisk()) {
            tooltip.add(Rs2Mod.createTranslation("misc", "stored", QuantityFormatter.format(stored)));
        } else {
            long capacity = getScreenHandler().getCapacity();
            double progress = getScreenHandler().getProgress();

            tooltip.add(Rs2Mod.createTranslation("misc", "stored_with_capacity", QuantityFormatter.format(stored), QuantityFormatter.format(capacity)));
            tooltip.add(Rs2Mod.createTranslation("misc", "full", (int) (progress * 100D)).formatted(Formatting.GRAY));
        }

        return tooltip;
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
        textRenderer.draw(matrices, DISKS_TEXT, 60, 42, 4210752);
        progressWidget.render(matrices, mouseX - x, mouseY - y, 0);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
