package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

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
}
