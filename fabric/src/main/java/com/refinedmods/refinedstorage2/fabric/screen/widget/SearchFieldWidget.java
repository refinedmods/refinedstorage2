package com.refinedmods.refinedstorage2.fabric.screen.widget;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2ClientMod;
import com.refinedmods.refinedstorage2.fabric.mixin.TextFieldWidgetAccessor;
import com.refinedmods.refinedstorage2.fabric.util.KeyBindingUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class SearchFieldWidget extends TextFieldWidget {
    private final History history;

    public SearchFieldWidget(TextRenderer textRenderer, int x, int y, int width, History history) {
        super(textRenderer, x, y, width, textRenderer.fontHeight, new LiteralText(""));

        this.history = history;
        this.setHasBorder(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean wasFocused = isFocused();
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean clickedWidget = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (clickedWidget && mouseButton == 1) {
            setText("");
            setFocused(true);
        } else if (wasFocused != isFocused()) {
            saveHistory();
        }

        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        boolean result = super.keyPressed(keyCode, scanCode, modifier);

        boolean focusUnlocked = ((TextFieldWidgetAccessor) this).getFocusUnlocked();

        if (isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                setText(history.older());
                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                setText(history.newer());
                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveHistory();

                if (focusUnlocked) {
                    setFocused(false);
                }

                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                saveHistory();

                setFocused(false);

                if (focusUnlocked) {
                    result = true;
                } else {
                    result = false;
                }
            }
        }

        if (KeyBindingUtil.isKeyDown(RefinedStorage2ClientMod.getFocusSearchBarKeyBinding()) && focusUnlocked) {
            setFocused(!isFocused());
            saveHistory();
            result = true;
        }

        return result;
    }

    private void saveHistory() {
        history.save(getText());
    }
}
