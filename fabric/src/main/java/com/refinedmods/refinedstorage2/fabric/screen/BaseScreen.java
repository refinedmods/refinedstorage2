package com.refinedmods.refinedstorage2.fabric.screen;

import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

public abstract class BaseScreen<T extends ScreenHandler> extends HandledScreen<T> {
    private int sideButtonY;

    public BaseScreen(T screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Override
    protected void init() {
        children.clear();
        buttons.clear();
        super.init();
        sideButtonY = 6;
    }

    public void addSideButton(SideButtonWidget button) {
        button.x = x - button.getWidth() - 2;
        button.y = y + sideButtonY;

        sideButtonY += button.getHeight() + 2;

        addButton(button);
    }

    protected void setScissor(int x, int y, int w, int h) {
        double scale = client.getWindow().getScaleFactor();
        int sx = (int) (x * scale);
        int sy = (int) ((client.getWindow().getScaledHeight() - (y + h)) * scale);
        int sw = (int) (w * scale);
        int sh = (int) (h * scale);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(sx, sy, sw, sh);
    }

    protected void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
