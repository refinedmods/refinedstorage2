package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.AlternativesScreen;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public abstract class AbstractAmountScreen<T extends AbstractContainerMenu, N extends Number>
    extends AbstractBaseScreen<T> {
    private static final MutableComponent RESET_TEXT = createTranslation("gui", "configure_amount.reset");
    private static final MutableComponent CANCEL_TEXT = Component.translatable("gui.cancel");

    private static final int INCREMENT_BUTTON_WIDTH = 30;
    private static final int ACTION_BUTTON_HEIGHT = 20;
    private static final int ACTION_BUTTON_WIDTH = 58;
    private static final int ACTION_BUTTON_SPACING = 20;

    @Nullable
    protected ActionButton confirmButton;
    @Nullable
    protected ActionButton cancelButton;
    @Nullable
    protected EditBox amountField;
    @Nullable
    protected N amount;

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
        this.amount = configuration.getInitialAmount();
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
        final Vector3f pos = configuration.getActionButtonsStartPosition();
        if (configuration.isHorizontalActionButtons()) {
            final int spacing = 3;
            addCancelButton((int) pos.x, (int) pos.y);
            final Button resetButton = addResetButton((int) pos.x + requireNonNull(cancelButton).getWidth() + spacing,
                (int) pos.y);
            addConfirmButton((int) pos.x + cancelButton.getWidth() + spacing + resetButton.getWidth() + spacing,
                (int) pos.y);
        } else {
            final int spacing = 24;
            addResetButton((int) pos.x, (int) pos.y);
            addConfirmButton((int) pos.x, (int) pos.y + spacing);
            addCancelButton((int) pos.x, (int) pos.y + spacing * 2);
        }
    }

    private Button addResetButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(RESET_TEXT) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final ActionButton button = new ActionButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
            RESET_TEXT,
            btn -> reset()
        );
        button.setIcon(ActionIcon.RESET);
        return addRenderableWidget(button);
    }

    private void addConfirmButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(configuration.getConfirmButtonText()) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final ActionButton button = new ActionButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
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

    private void addCancelButton(final int x, final int y) {
        final int width = configuration.isHorizontalActionButtons()
            ? font.width(CANCEL_TEXT) + ACTION_BUTTON_SPACING + ICON_SIZE
            : ACTION_BUTTON_WIDTH;
        final ActionButton button = new ActionButton(
            leftPos + x,
            topPos + y,
            width,
            ACTION_BUTTON_HEIGHT,
            CANCEL_TEXT,
            btn -> close()
        );
        button.setIcon(ActionIcon.CANCEL);
        cancelButton = addRenderableWidget(button);
    }

    private void addAmountField() {
        final Vector3f pos = configuration.getAmountFieldPosition();
        amountField = new EditBox(
            font,
            leftPos + (int) pos.x(),
            topPos + (int) pos.y(),
            configuration.getAmountFieldWidth() - 6,
            font.lineHeight,
            Component.empty()
        );
        amountField.setBordered(false);
        amountField.setTextColor(0xFFFFFF);
        updateAmount(this.amount);
        onAmountFieldChanged();
        amountField.setVisible(true);
        amountField.setCanLoseFocus(this instanceof AlternativesScreen);
        amountField.setFocused(true);
        amountField.setResponder(value -> onAmountFieldChanged());
        setFocused(amountField);

        addRenderableWidget(amountField);
    }

    protected final void updateAmount(@Nullable final N value) {
        if (amountField == null) {
            return;
        }
        amountField.setValue(amountOperations.format(value));
        this.amount = value;
    }

    protected void onAmountFieldChanged() {
        if (amountField == null) {
            return;
        }
        final AmountOperations.ReturnValue<N> data = checkValue();
        final boolean valid = data.getValue().isPresent();
        if (confirmButton != null) {
            confirmButton.active = valid;
            confirmButton.setIcon(valid ? getConfirmButtonIcon() : ActionIcon.ERROR);
        } else {
            tryConfirm();
        }
        this.amount = (data.getValue().isEmpty()) ? null : data.getValue().get();
        amountField.setTextColor(valid ? 0xFFFFFF : 0xFF5555);
        setToolTip(data.getTooltip(), amountField);
    }

    protected void setToolTip(final String text, @Nullable final AbstractWidget element) {
        final Component tooltip;
        if (element != null) {
            //tooltips have to be separate to show min and max values
            if (text.startsWith("resource_amount_input.too_big")) {
                tooltip = createTranslation("gui",
                    "resource_amount_input.too_big",
                    amountOperations.format(configuration.getMaxAmount()));
            } else if (text.startsWith("resource_amount_input.too_small")) {
                tooltip = createTranslation("gui",
                    "resource_amount_input.too_small",
                    amountOperations.format(configuration.getMinAmount()));
            } else if (text.startsWith("resource_amount_input")) {
                tooltip = createTranslation("gui", text);
            } else {
                tooltip = Component.nullToEmpty(text);
            }
            element.setTooltip(Tooltip.create(tooltip));
        }
    }

    private void addIncrementButtons() {
        final Vector3f incrementsTopPos = configuration.getIncrementsTopStartPosition();
        addIncrementButtons(
            configuration.getIncrementsTop(),
            leftPos + (int) incrementsTopPos.x,
            topPos + (int) incrementsTopPos.y
        );
        final Vector3f incrementsBottomPos = configuration.getIncrementsBottomStartPosition();
        addIncrementButtons(
            configuration.getIncrementsBottom(),
            leftPos + (int) incrementsBottomPos.x,
            topPos + (int) incrementsBottomPos.y
        );
    }

    private void addIncrementButtons(final int[] increments, final int x, final int y) {
        for (int i = 0; i < increments.length; ++i) {
            final int increment = increments[i];
            final int xx = x + ((INCREMENT_BUTTON_WIDTH + 3) * i);
            addRenderableWidget(createIncrementButton(xx, y, increment));
        }
    }

    protected abstract boolean confirm(N value);

    private Button createIncrementButton(final int x, final int y, final int increment) {
        final Component text = Component.literal((increment > 0 ? "+" : "") + increment);
        return Button.builder(text, btn -> changeAmount(increment))
            .pos(x, y)
            .size(INCREMENT_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
            .build();
    }

    private void changeAmount(final int delta) {
        if (amountField == null) {
            return;
        }
        final int correctedDelta = correctDelta(amount, delta);
        final N newAmount = amountOperations.changeAmount(
            amount,
            correctedDelta,
            configuration.getMinAmount(),
            configuration.getMaxAmount()
        );
        updateAmount(newAmount);
    }

    private int correctDelta(@Nullable final N oldAmount, final int delta) {
        // if we do +10, and the current value is 1, we want to end up with 10, not 11
        // if we do +1, and the current value is 1, we want to end up with 2
        if (oldAmount == null) {
            return delta;
        }
        if (oldAmount.intValue() == 1 && delta > 1) {
            return delta - 1;
        }
        return delta;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        if (delta > 0) {
            changeAmount(1);
        } else {
            changeAmount(-1);
        }
        return super.mouseScrolled(x, y, z, delta);
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
        checkValue().getValue().ifPresent(this::confirm);
    }

    private void tryConfirmAndCloseToParent() {
        checkValue().getValue().ifPresent(value -> {
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

    protected AmountOperations.ReturnValue<N> checkValue() {
        if (amountField == null) {
            return new AmountOperations.ReturnValue<>("resource_amount_input.no_input");
        }

        @Nullable final String value = amountField.getValue();

        if (value.isEmpty()) {
            return new AmountOperations.ReturnValue<>("resource_amount_input.no_input");
        }
        return amountOperations.calculate(value,
            configuration.getMinAmount(),
            configuration.getMaxAmount()
        );
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
