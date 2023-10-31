package com.refinedmods.refinedstorage2.platform.common.support.widget;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SearchFieldWidget extends EditBox {
    private final History history;

    public SearchFieldWidget(final Font textRenderer,
                             final int x,
                             final int y,
                             final int width,
                             final History history) {
        super(textRenderer, x, y, width, textRenderer.lineHeight, Component.empty());
        this.history = history;
        this.setBordered(false);
        this.setMaxLength(256);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        final boolean wasFocused = isFocused();
        final boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        final boolean clickedWidget = mouseX >= getX()
            && mouseX < getX() + width
            && mouseY >= getY()
            && mouseY < getY() + height;

        if (clickedWidget && mouseButton == 1) {
            setValue("");
            setFocused(true);
        } else if (wasFocused != isFocused()) {
            saveHistory();
        }

        return result;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifier) {
        final boolean canLoseFocus = Platform.INSTANCE.canEditBoxLoseFocus(this);
        if (isFocused() && handleKeyCode(keyCode, canLoseFocus)) {
            return true;
        }
        if (Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getFocusSearchBar()) && canLoseFocus) {
            return handleFocusToggle();
        }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    private boolean handleKeyCode(final int keyCode,
                                  final boolean canLoseFocus) {
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            final String newValue = keyCode == GLFW.GLFW_KEY_UP ? history.older() : history.newer();
            setValue(newValue);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveHistory();
            if (canLoseFocus) {
                setFocused(false);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            saveHistory();
            setFocused(false);
            // If we can't lose focus, "fail" and let bubble it up so that the screen can be closed.
            return canLoseFocus;
        }
        return false;
    }

    private boolean handleFocusToggle() {
        setFocused(!isFocused());
        saveHistory();
        return true;
    }

    private void saveHistory() {
        history.save(getValue());
    }
}
