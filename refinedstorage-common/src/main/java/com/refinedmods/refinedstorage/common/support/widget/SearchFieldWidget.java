package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.KeyMappings;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SearchFieldWidget extends EditBox {
    private final History history;

    public SearchFieldWidget(final Font font,
                             final int x,
                             final int y,
                             final int width,
                             final History history) {
        super(font, x, y, width, font.lineHeight, Component.empty());
        this.history = history;
        setBordered(false);
        setMaxLength(256);
        setAutoSelected(Platform.INSTANCE.getConfig().isSearchBoxAutoSelected());
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        final boolean wasFocused = isFocused();
        final boolean result = super.mouseClicked(event, doubleClick);
        final double mouseX = event.x();
        final double mouseY = event.y();
        final boolean clickedWidget = mouseX >= getX()
            && mouseX < getX() + width
            && mouseY >= getY()
            && mouseY < getY() + height;
        setFocused(clickedWidget);
        if (clickedWidget && event.button() == 1) {
            setValue("");
        } else if (wasFocused != isFocused()) {
            saveHistory();
        }
        return result;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        final boolean canLoseFocus = Platform.INSTANCE.canEditBoxLoseFocus(this);
        // The search field takes control over everything if it's active and focused.
        // Calculate this here because "shouldMoveControlToParent" may change the focus.
        final boolean havingControl = isActive() && isFocused();
        // Sometimes pressing a special key (like ESC) should return control to the parent.
        if (havingControl && shouldMoveControlToParent(event.key(), canLoseFocus)) {
            return false;
        }
        if (historyRelatedKeyPressed(event.key())) {
            return false;
        }
        if (Platform.INSTANCE.isKeyDown(KeyMappings.INSTANCE.getFocusSearchBar()) && canLoseFocus) {
            toggleFocus();
        }
        // Call the parent to process more special characters.
        super.keyPressed(event);
        return havingControl;
    }

    private boolean historyRelatedKeyPressed(final int keyCode) {
        return keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN
            || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
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
            // If we are not autoselected, we can just lose focus (which will require another ESC press to close).
            return !canLoseFocus;
        }
        return false;
    }

    private void toggleFocus() {
        setFocused(!isFocused());
        saveHistory();
    }

    public void setAutoSelected(final boolean autoSelected) {
        setFocused(autoSelected);
        setCanLoseFocus(!autoSelected);
    }

    private void saveHistory() {
        history.save(getValue());
    }
}
