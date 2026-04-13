package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
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
        this.treePreviewWidget = new TreePreviewWidget(0, 0, width, height);
        this.treePreviewWidget.update(treePreview);
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        if (treePreviewWidget == null) {
            return;
        }
        treePreviewWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
        if (treePreviewWidget != null) {
            return treePreviewWidget.mouseDragged(event, dx, dy);
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (treePreviewWidget == null) {
            return super.mouseScrolled(x, y, scrollX, scrollY);
        }
        return treePreviewWidget.mouseScrolled(x, y, scrollX, scrollY);
    }
}
