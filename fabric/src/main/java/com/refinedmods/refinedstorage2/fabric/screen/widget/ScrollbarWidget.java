package com.refinedmods.refinedstorage2.fabric.screen.widget;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScrollbarWidget extends DrawableHelper implements Element, Drawable {
    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/widgets.png");
    private static final int SCROLLER_HEIGHT = 15;

    private final MinecraftClient client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private int offset;
    private int maxOffset;
    private boolean enabled = true;
    private boolean clicked;

    public ScrollbarWidget(MinecraftClient client, int x, int y, int width, int height) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrixStack, x, y + (int) ((float) offset / (float) maxOffset * (float) (height - SCROLLER_HEIGHT)), enabled ? 232 : 244, 0, 12, 15);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (clicked && mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height) {
            updateOffset(mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height) {
            updateOffset(mouseY);
            clicked = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (clicked) {
            clicked = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (enabled) {
            setOffset(offset + Math.max(Math.min(-(int) scrollDelta, 1), -1));
            return true;
        }
        return false;
    }

    public void setMaxOffset(int maxOffset) {
        this.maxOffset = maxOffset;

        if (offset > maxOffset) {
            this.offset = Math.max(0, maxOffset);
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        if (offset >= 0 && offset <= maxOffset) {
            this.offset = offset;
        }
    }

    private void updateOffset(double mouseY) {
        setOffset((int) Math.floor((float) (mouseY - y) / (float) (height - SCROLLER_HEIGHT) * (float) maxOffset));
    }
}
