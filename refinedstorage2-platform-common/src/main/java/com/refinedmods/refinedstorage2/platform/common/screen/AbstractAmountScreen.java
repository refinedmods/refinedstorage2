package com.refinedmods.refinedstorage2.platform.common.screen;

import java.util.Optional;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractAmountScreen extends AbstractBaseScreen<AbstractContainerMenu> {
    private static final MutableComponent SET_TEXT = createTranslation("gui", "amount.set");
    private static final MutableComponent RESET_TEXT = createTranslation("gui", "amount.reset");
    private static final MutableComponent CANCEL_TEXT = Component.translatable("gui.cancel");

    private static final int INCREMENT_BUTTON_WIDTH = 30;
    private static final int INCREMENT_BUTTON_X = 7;
    private static final int INCREMENT_BUTTON_TOP_Y = 20;

    private static final int ACTION_BUTTON_WIDTH = 50;

    private final Screen parent;

    @Nullable
    private EditBox amountField;
    @Nullable
    private Button confirmButton;

    protected AbstractAmountScreen(final Screen parent, final Inventory playerInventory, final Component title) {
        this(new DefaultDummyContainerMenu(), parent, playerInventory, title);
    }

    protected AbstractAmountScreen(final AbstractContainerMenu containerMenu,
                                   final Screen parent,
                                   final Inventory playerInventory,
                                   final Component title) {
        super(containerMenu, playerInventory, title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        addActionButtons();
        addAmountField();
        addIncrementButtons(getIncrementsTop(), leftPos + INCREMENT_BUTTON_X, topPos + INCREMENT_BUTTON_TOP_Y);
        addIncrementButtons(getIncrementsBottom(), leftPos + INCREMENT_BUTTON_X, topPos + imageHeight - 27);
    }

    private void addActionButtons() {
        final Vector3f actionButtonPos = getActionButtonPosition();
        addRenderableWidget(new Button(
            leftPos + (int) actionButtonPos.x(),
            topPos + (int) actionButtonPos.y(),
            ACTION_BUTTON_WIDTH,
            20,
            RESET_TEXT,
            btn -> reset()
        ));
        confirmButton = addRenderableWidget(new Button(
            leftPos + (int) actionButtonPos.x(),
            topPos + (int) actionButtonPos.y() + 24,
            ACTION_BUTTON_WIDTH,
            20,
            SET_TEXT,
            btn -> tryConfirm()
        ));
        addRenderableWidget(new Button(
            leftPos + (int) actionButtonPos.x(),
            topPos + (int) actionButtonPos.y() + 48,
            ACTION_BUTTON_WIDTH,
            20,
            CANCEL_TEXT,
            btn -> close()
        ));
    }

    private void addAmountField() {
        final Vector3f pos = getAmountFieldPosition();

        amountField = new EditBox(
            font,
            leftPos + (int) pos.x(),
            topPos + (int) pos.y(),
            69 - 6,
            font.lineHeight,
            Component.empty()
        );
        amountField.setBordered(false);
        amountField.setValue(String.valueOf(getInitialAmount()));
        amountField.setVisible(true);
        amountField.setCanLoseFocus(false);
        amountField.setFocus(true);
        amountField.setResponder(value -> {
            if (confirmButton != null) {
                confirmButton.active = getAndValidateAmount().isPresent();
            }
        });

        addRenderableWidget(amountField);
    }

    protected abstract int getInitialAmount();

    protected abstract int[] getIncrementsTop();

    protected abstract int[] getIncrementsBottom();

    protected abstract Vector3f getAmountFieldPosition();

    protected abstract Vector3f getActionButtonPosition();

    protected abstract int getMinAmount();

    protected abstract int getMaxAmount();

    protected abstract int getResetAmount();

    protected abstract void accept(int amount);

    private void addIncrementButtons(final int[] increments, final int x, final int y) {
        for (int i = 0; i < increments.length; ++i) {
            final int increment = increments[i];
            final Component text = Component.literal((increment > 0 ? "+" : "") + increment);
            final int xx = x + ((INCREMENT_BUTTON_WIDTH + 3) * i);
            addRenderableWidget(new Button(xx, y, INCREMENT_BUTTON_WIDTH, 20, text, btn -> changeAmount(increment)));
        }
    }

    private void changeAmount(final int delta) {
        if (amountField == null) {
            return;
        }
        getAndValidateAmount().ifPresent(oldAmount -> {
            final int newAmount = oldAmount + delta;
            final int correctedNewAmount = Mth.clamp(newAmount, getMinAmount(), getMaxAmount());
            amountField.setValue(String.valueOf(correctedNewAmount));
        });
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double delta) {
        if (delta > 0) {
            changeAmount(1);
        } else {
            changeAmount(-1);
        }
        return super.mouseScrolled(x, y, delta);
    }

    @Override
    protected void renderLabels(final PoseStack poseStack, final int mouseX, final int mouseY) {
        font.draw(poseStack, title, titleLabelX, titleLabelY, 4210752);
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (amountField != null && amountField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (amountField != null
            && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
            && amountField.isFocused()) {
            tryConfirm();
            return true;
        }
        if (amountField != null
            && (amountField.keyPressed(key, scanCode, modifiers) || amountField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private void reset() {
        if (amountField == null) {
            return;
        }
        amountField.setValue("" + getResetAmount());
    }

    private void tryConfirm() {
        getAndValidateAmount().ifPresent(this::confirm);
    }

    private void confirm(final int amount) {
        accept(amount);
        close();
    }

    private void close() {
        Minecraft.getInstance().setScreen(parent);
    }

    private Optional<Integer> getAndValidateAmount() {
        if (amountField == null) {
            return Optional.empty();
        }
        try {
            final int amount = Integer.parseInt(amountField.getValue());
            return validateAmount(amount);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> validateAmount(final int amount) {
        if (amount >= getMinAmount() && amount <= getMaxAmount()) {
            return Optional.of(amount);
        } else {
            return Optional.empty();
        }
    }

    private static class DefaultDummyContainerMenu extends AbstractContainerMenu {
        protected DefaultDummyContainerMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
