package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.core.History;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class SearchFieldWidget extends EditBox {
    private final History history;

    public SearchFieldWidget(Font textRenderer, int x, int y, int width, History history) {
        super(textRenderer, x, y, width, textRenderer.lineHeight, new TextComponent(""));

        this.history = history;
        this.setBordered(false);
        this.setMaxLength(256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean wasFocused = isFocused();
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean clickedWidget = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        if (clickedWidget && mouseButton == 1) {
            setValue("");
            setFocused(true);
        } else if (wasFocused != isFocused()) {
            saveHistory();
        }

        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        boolean result = super.keyPressed(keyCode, scanCode, modifier);

        boolean canLoseFocus = Platform.INSTANCE.canEditBoxLoseFocus(this);

        if (isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                setValue(history.older());
                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                setValue(history.newer());
                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveHistory();

                if (canLoseFocus) {
                    setFocused(false);
                }

                result = true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                saveHistory();

                setFocused(false);

                if (canLoseFocus) {
                    result = true;
                } else {
                    result = false;
                }
            }
        }

        if (Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getFocusSearchBar()) && canLoseFocus) {
            setFocused(!isFocused());
            saveHistory();
            result = true;
        }

        return result;
    }

    private void saveHistory() {
        history.save(getValue());
    }
}
