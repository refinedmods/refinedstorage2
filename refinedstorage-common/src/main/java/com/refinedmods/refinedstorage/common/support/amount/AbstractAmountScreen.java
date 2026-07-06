package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractAmountScreen<T extends AbstractContainerMenu, N extends Number>
    extends AbstractBaseScreen<T> {
    static final MutableComponent RESET_TEXT = createTranslation("gui", "configure_amount.reset");
    static final MutableComponent CANCEL_TEXT = Component.translatable("gui.cancel");

    private static final int INCREMENT_BUTTON_WIDTH = 30;

    @Nullable
    protected ActionButton confirmButton;
    @Nullable
    protected ActionButton cancelButton;
    @Nullable
    protected EditBox amountField;

    @Nullable
    private final Screen parent;
    private final AmountScreenConfiguration<N> configuration;
    private final AmountOperations<N> amountOperations;

    protected AbstractAmountScreen(final T containerMenu,
                                   @Nullable final Screen parent,
                                   final Inventory playerInventory,
                                   final Component title,
                                   final AmountScreenConfiguration<N> configuration,
                                   final AmountOperations<N> amountOperations) {
        super(containerMenu, playerInventory, title);
        this.parent = parent;
        this.configuration = configuration;
        this.amountOperations = amountOperations;
    }

    @Override
    protected void init() {
        super.init();
        if (configuration.isActionButtonsEnabled()) {
            addActionButtons();
        }
        addAmountField();
        addIncrementButtons();
    }

    private void addActionButtons() {
        addResetButton(configuration.getActionButtonPositions().reset());
        addCancelButton(configuration.getActionButtonPositions().cancel());
        addConfirmButton(configuration.getActionButtonPositions().confirm());
    }

    private void addResetButton(final AmountScreenConfiguration.ActionButtonPositionAndSize dim) {
        final ActionButton button = new ActionButton(
            leftPos + dim.pos().x,
            topPos + dim.pos().y,
            dim.size().x,
            dim.size().y,
            RESET_TEXT,
            btn -> reset()
        );
        button.setIcon(ActionIcon.RESET);
        addRenderableWidget(button);
    }

    private void addConfirmButton(final AmountScreenConfiguration.ActionButtonPositionAndSize dim) {
        final ActionButton button = new ActionButton(
            leftPos + dim.pos().x,
            topPos + dim.pos().y,
            dim.size().x,
            dim.size().y,
            configuration.getConfirmButtonText(),
            btn -> tryConfirmAndCloseToParent()
        );
        button.setIcon(getConfirmButtonIcon());
        confirmButton = addRenderableWidget(button);
    }

    @Nullable
    protected ActionIcon getConfirmButtonIcon() {
        return ActionIcon.SET;
    }

    private void addCancelButton(final AmountScreenConfiguration.ActionButtonPositionAndSize dim) {
        final ActionButton button = new ActionButton(
            leftPos + dim.pos().x,
            topPos + dim.pos().y,
            dim.size().x,
            dim.size().y,
            CANCEL_TEXT,
            btn -> close()
        );
        button.setIcon(ActionIcon.CANCEL);
        cancelButton = addRenderableWidget(button);
    }

    private void addAmountField() {
        final Vector2i pos = configuration.getAmountFieldPosition();
        final String originalValue = amountField != null ? amountField.getValue() : null;
        amountField = new EditBox(
            font,
            leftPos + pos.x(),
            topPos + pos.y(),
            configuration.getAmountFieldWidth() - 6,
            font.lineHeight,
            Component.empty()
        );
        amountField.setBordered(false);
        amountField.setTextColor(0xFFFFFF);
        if (originalValue != null) {
            amountField.setValue(originalValue);
            onAmountFieldChanged();
        } else if (configuration.getInitialAmount() != null) {
            updateAmount(configuration.getInitialAmount());
        }
        amountField.setVisible(true);
        amountField.setCanLoseFocus(this instanceof AlternativesScreen);
        amountField.setFocused(true);
        amountField.setResponder(value -> onAmountFieldChanged());
        setFocused(amountField);

        addRenderableWidget(amountField);
    }

    protected final void updateAmount(final N amount) {
        if (amountField == null) {
            return;
        }
        amountField.setValue(amountOperations.format(amount));
    }

    protected void onAmountFieldChanged() {
        if (amountField == null) {
            return;
        }
        final boolean valid = getAndValidateAmount().isPresent();
        if (confirmButton != null) {
            confirmButton.active = valid;
            confirmButton.setIcon(valid ? getConfirmButtonIcon() : ActionIcon.ERROR);
        } else {
            tryConfirm();
        }
        amountField.setTextColor(valid ? 0xFFFFFF : 0xFF5555);
    }

    private void addIncrementButtons() {
        final Vector2i incrementsTopPos = configuration.getIncrementsTopStartPosition();
        addIncrementButtons(
            configuration.getIncrementsTop(),
            leftPos + incrementsTopPos.x,
            topPos + incrementsTopPos.y
        );
        final Vector2i incrementsBottomPos = configuration.getIncrementsBottomStartPosition();
        addIncrementButtons(
            configuration.getIncrementsBottom(),
            leftPos + incrementsBottomPos.x,
            topPos + incrementsBottomPos.y
        );
    }

    private void addIncrementButtons(final int[] increments, final int x, final int y) {
        for (int i = 0; i < increments.length; ++i) {
            final int increment = increments[i];
            final int xx = x + ((INCREMENT_BUTTON_WIDTH + 3) * i);
            addRenderableWidget(createIncrementButton(xx, y, increment));
        }
    }

    protected abstract boolean confirm(N amount);

    private Button createIncrementButton(final int x, final int y, final int increment) {
        final Component text = Component.literal((increment > 0 ? "+" : "") + increment);
        return Button.builder(text, btn -> changeAmount(increment))
            .pos(x, y)
            .size(INCREMENT_BUTTON_WIDTH, 20)
            .build();
    }

    private void changeAmount(final int delta) {
        if (amountField == null) {
            return;
        }
        getAndValidateAmount().ifPresentOrElse(oldAmount -> {
            final int correctedDelta = correctDelta(oldAmount, delta);
            final N newAmount = amountOperations.changeAmount(
                oldAmount,
                correctedDelta,
                configuration.getMinAmount(),
                configuration.getMaxAmount()
            );
            updateAmount(newAmount);
        }, () -> updateAmount(amountOperations.changeAmount(
            null,
            delta,
            configuration.getMinAmount(),
            configuration.getMaxAmount()
        )));
    }

    private int correctDelta(final N oldAmount, final int delta) {
        // if we do +10, and the current value is 1, we want to end up with 10, not 11
        // if we do +1, and the current value is 1, we want to end up with 2
        if (oldAmount.intValue() == 1 && delta > 1) {
            return delta - 1;
        }
        return delta;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (scrollY > 0) {
            changeAmount(1);
        } else {
            changeAmount(-1);
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (amountField != null && amountField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (tryClose(key)) {
            return true;
        }
        if (amountField != null
            && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)
            && amountField.isFocused()) {
            tryConfirmAndCloseToParent();
            return true;
        }
        if (amountField != null
            && (amountField.keyPressed(key, scanCode, modifiers) || amountField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    protected final boolean tryClose(final int key) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    protected void reset() {
        if (amountField == null || configuration.getResetAmount() == null) {
            return;
        }
        updateAmount(configuration.getResetAmount());
    }

    private void tryConfirm() {
        getAndValidateAmount().ifPresent(this::confirm);
    }

    private void tryConfirmAndCloseToParent() {
        getAndValidateAmount().ifPresent(value -> {
            if (confirm(value)) {
                tryCloseToParent();
            }
        });
    }

    private boolean tryCloseToParent() {
        if (parent != null) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return false;
    }

    public final void close() {
        if (!beforeClose()) {
            return;
        }
        if (!tryCloseToParent()) {
            onClose();
        }
    }

    protected boolean beforeClose() {
        return true;
    }

    protected final Optional<N> getAndValidateAmount() {
        if (amountField == null) {
            return Optional.empty();
        }
        return amountOperations.parse(amountField.getValue()).flatMap(amount -> amountOperations.validate(
            amount,
            configuration.getMinAmount(),
            configuration.getMaxAmount()
        ));
    }

    protected static class DefaultDummyContainerMenu extends AbstractContainerMenu {
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
