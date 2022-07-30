package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractBaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private int sideButtonY;

    protected AbstractBaseScreen(final T menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        sideButtonY = 6;
    }

    @Override
    protected void renderBg(final PoseStack poseStack, final float delta, final int mouseX, final int mouseY) {
        renderResourceFilterSlots(poseStack);
    }

    protected void renderResourceFilterSlots(final PoseStack poseStack) {
        for (final Slot slot : menu.slots) {
            if (slot instanceof ResourceFilterSlot resourceFilterSlot) {
                resourceFilterSlot.render(poseStack, leftPos + slot.x, topPos + slot.y, getBlitOffset());
            }
        }
    }

    public void addSideButton(final AbstractSideButtonWidget button) {
        button.x = leftPos - button.getWidth() - 2;
        button.y = topPos + sideButtonY;

        sideButtonY += button.getHeight() + 2;

        addRenderableWidget(button);
    }

    protected void setScissor(final int x, final int y, final int w, final int h) {
        if (minecraft == null) {
            return;
        }
        final double scale = minecraft.getWindow().getGuiScale();
        final int sx = (int) (x * scale);
        final int sy = (int) ((minecraft.getWindow().getGuiScaledHeight() - (y + h)) * scale);
        final int sw = (int) (w * scale);
        final int sh = (int) (h * scale);
        RenderSystem.enableScissor(sx, sy, sw, sh);
    }

    @Override
    protected void renderTooltip(final PoseStack poseStack, final int x, final int y) {
        super.renderTooltip(poseStack, x, y);
        if (minecraft != null
            && menu.getCarried().isEmpty()
            && hoveredSlot instanceof ResourceFilterSlot resourceFilterSlot) {
            final List<Component> lines = resourceFilterSlot.getTooltipLines(minecraft.player);
            if (!lines.isEmpty()) {
                this.renderComponentTooltip(poseStack, lines, x, y);
            }
        }
    }
}
