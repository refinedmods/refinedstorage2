package com.refinedmods.refinedstorage2.fabric.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage2.core.util.Quantities;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ExactModeSideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ProgressWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class DiskDriveScreen extends BaseScreen<DiskDriveScreenHandler> {
    private static final Identifier TEXTURE = RefinedStorage2Mod.createIdentifier("textures/gui/disk_drive.png");
    private static final TranslatableText DISKS_TEXT = RefinedStorage2Mod.createTranslation("gui", "disk_drive.disks");

    private final ProgressWidget progressWidget;

    public DiskDriveScreen(DiskDriveScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 129;
        this.backgroundWidth = 176;
        this.backgroundHeight = 223;

        this.progressWidget = new ProgressWidget(99, 54, 16, 70, handler::getProgress, this::renderTooltip, this::createTooltip);
        addChild(progressWidget);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new ExactModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new AccessModeSideButtonWidget(getScreenHandler(), this::renderTooltip));
        addSideButton(new PrioritySideButtonWidget(getScreenHandler(), this, this::renderTooltip));
    }

    private List<Text> createTooltip() {
        List<Text> tooltip = new ArrayList<>();
        int stored = getScreenHandler().getStored();

        if (handler.hasInfiniteDisk()) {
            tooltip.add(RefinedStorage2Mod.createTranslation("misc", "stored", Quantities.format(stored)));
        } else {
            int capacity = getScreenHandler().getCapacity();
            double progress = getScreenHandler().getProgress();

            tooltip.add(RefinedStorage2Mod.createTranslation("misc", "stored_with_capacity", Quantities.format(stored), Quantities.format(capacity)));
            tooltip.add(RefinedStorage2Mod.createTranslation("misc", "full", (int) (progress * 100D)).formatted(Formatting.GRAY));
        }

        return tooltip;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ScreenUtil.drawVersionInformation(matrices, textRenderer, delta);
        client.getTextureManager().bindTexture(TEXTURE);
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
