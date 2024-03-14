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
        // The search field takes control over everything if it's active and focused.
        // Calculate this here because "shouldMoveControlToParent" may change the focus.
        final boolean havingControl = isActive() && isFocused();
        // Sometimes pressing a special key (like ESC) should return control to the parent.
        if (havingControl && shouldMoveControlToParent(keyCode, canLoseFocus)) {
            return false;
        }
        if (Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getFocusSearchBar()) && canLoseFocus) {
            toggleFocus();
        }
        // Call the parent to process more special characters.
        super.keyPressed(keyCode, scanCode, modifier);
        return havingControl;
    }

    private boolean shouldMoveControlToParent(final int keyCode, final boolean canLoseFocus) {
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            final String newValue = keyCode == GLFW.GLFW_KEY_UP ? history.older() : history.newer();
            setValue(newValue);
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveHistory();
            if (canLoseFocus) {
                setFocused(false);
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            saveHistory();
            setFocused(false);
            // If we are autoselected, we need to move control back to the parent straight away.
            // If we are not autoselected, we can just unfocus (which will require another ESC press to close).
            return !canLoseFocus;
        }
        return false;
    }

    private void toggleFocus() {
        setFocused(!isFocused());
        saveHistory();
    }

    private void saveHistory() {
        history.save(getValue());
    }
}
