package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

class FullscreenTreePreviewScreen extends Screen {
    private final Screen parent;
    private final TreePreview treePreview;

    @Nullable
    private TreePreviewWidget treePreviewWidget;

    FullscreenTreePreviewScreen(final Screen parent, final TreePreview treePreview) {
        super(Component.empty());
        this.parent = parent;
        this.treePreview = treePreview;
    }

    @Override
    protected void init() {
        super.init();
        this.treePreviewWidget = new TreePreviewWidget(this, 0, 0, width, height);
        this.treePreviewWidget.update(treePreview);
    }

    @Override
    public void renderBackground(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                 final float partialTick) {
        if (treePreviewWidget == null) {
            return;
        }
        treePreviewWidget.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX,
                                final double dragY) {
        if (treePreviewWidget == null) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return treePreviewWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (treePreviewWidget == null) {
            return super.mouseScrolled(x, y, scrollX, scrollY);
        }
        return treePreviewWidget.mouseScrolled(x, y, scrollX, scrollY);
    }
}
