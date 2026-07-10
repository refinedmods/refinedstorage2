package com.refinedmods.refinedstorage.common.support.amount;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;
import static com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen.CANCEL_TEXT;
import static com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen.RESET_TEXT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public final class AmountScreenConfiguration<T extends Number> {
    private static final int ACTION_BUTTON_HEIGHT = 20;
    private static final int VERTICAL_ACTION_BUTTONS_FIXED_WIDTH = 58;
    private static final int ACTION_BUTTON_ICON_SPACING = 4 * 2;
    private static final int ACTION_BUTTON_VERTICAL_SPACING = 4;

    private static final MutableComponent SET_TEXT = createTranslation("gui", "configure_amount.set");

    @Nullable
    private final T initialAmount;
    private final int[] incrementsTop;
    private final Vector2i incrementsTopStartPosition;
    private final int[] incrementsBottom;
    private final Vector2i incrementsBottomStartPosition;
    private final int amountFieldWidth;
    private final Vector2i amountFieldPosition;
    private final ActionButtonPositions actionButtonPositions;
    private final boolean actionButtonsEnabled;
    private final Component confirmButtonText;
    @Nullable
    private final Supplier<T> minAmount;
    @Nullable
    private final T maxAmount;
    @Nullable
    private final T resetAmount;

    private AmountScreenConfiguration(@Nullable final T initialAmount,
                                      final int[] incrementsTop,
                                      final Vector2i incrementsTopStartPosition,
                                      final int[] incrementsBottom,
                                      final Vector2i incrementsBottomStartPosition,
                                      final int amountFieldWidth,
                                      final Vector2i amountFieldPosition,
                                      final ActionButtonPositions actionButtonPositions,
                                      final boolean actionButtonsEnabled,
                                      final Component confirmButtonText,
                                      @Nullable final Supplier<T> minAmount,
                                      @Nullable final T maxAmount,
                                      @Nullable final T resetAmount) {
        this.initialAmount = initialAmount;
        this.incrementsTop = incrementsTop;
        this.incrementsTopStartPosition = incrementsTopStartPosition;
        this.incrementsBottom = incrementsBottom;
        this.incrementsBottomStartPosition = incrementsBottomStartPosition;
        this.amountFieldWidth = amountFieldWidth;
        this.amountFieldPosition = amountFieldPosition;
        this.actionButtonPositions = actionButtonPositions;
        this.actionButtonsEnabled = actionButtonsEnabled;
        this.confirmButtonText = confirmButtonText;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.resetAmount = resetAmount;
    }

    @Nullable
    public T getInitialAmount() {
        return initialAmount;
    }

    public int[] getIncrementsTop() {
        return incrementsTop;
    }

    public Vector2i getIncrementsTopStartPosition() {
        return incrementsTopStartPosition;
    }

    public int[] getIncrementsBottom() {
        return incrementsBottom;
    }

    public Vector2i getIncrementsBottomStartPosition() {
        return incrementsBottomStartPosition;
    }

    public int getAmountFieldWidth() {
        return amountFieldWidth;
    }

    public Vector2i getAmountFieldPosition() {
        return amountFieldPosition;
    }

    public ActionButtonPositions getActionButtonPositions() {
        return actionButtonPositions;
    }

    public boolean isActionButtonsEnabled() {
        return actionButtonsEnabled;
    }

    public Component getConfirmButtonText() {
        return confirmButtonText;
    }

    @Nullable
    public T getMinAmount() {
        return minAmount != null ? minAmount.get() : null;
    }

    @Nullable
    public T getMaxAmount() {
        return maxAmount;
    }

    @Nullable
    public T getResetAmount() {
        return resetAmount;
    }

    public static final class AmountScreenConfigurationBuilder<T extends Number> {
        @Nullable
        private T initialAmount;
        private int[] incrementsTop = new int[] {};
        private Vector2i incrementsTopStartPosition = new Vector2i(7, 20);
        private int[] incrementsBottom = new int[] {};
        private Vector2i incrementsBottomStartPosition = new Vector2i(7, 67);
        private int amountFieldWidth = 68;
        private Vector2i amountFieldPosition = new Vector2i(0, 0);
        private ActionButtonPositionAndSize cancelButton = ActionButtonPositionAndSize.ZERO;
        private ActionButtonPositionAndSize resetButton = ActionButtonPositionAndSize.ZERO;
        private ActionButtonPositionAndSize confirmButton = ActionButtonPositionAndSize.ZERO;
        private Component confirmButtonText = SET_TEXT;
        private boolean actionButtonsEnabled = true;
        @Nullable
        private Supplier<T> minAmount;
        @Nullable
        private T maxAmount;
        @Nullable
        private T resetAmount;

        private AmountScreenConfigurationBuilder() {
        }

        public static <T extends Number> AmountScreenConfigurationBuilder<T> create() {
            return new AmountScreenConfigurationBuilder<>();
        }

        public AmountScreenConfigurationBuilder<T> withInitialAmount(final T newInitialAmount) {
            this.initialAmount = newInitialAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsTop(final int... newIncrementsTop) {
            this.incrementsTop = newIncrementsTop;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsTopStartPosition(final int x, final int y) {
            this.incrementsTopStartPosition = new Vector2i(x, y);
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsBottom(final int... newIncrementsBottom) {
            this.incrementsBottom = newIncrementsBottom;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsBottomStartPosition(final int x, final int y) {
            this.incrementsBottomStartPosition = new Vector2i(x, y);
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withAmountFieldWidth(final int newAmountFieldWidth) {
            this.amountFieldWidth = newAmountFieldWidth;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withAmountFieldPosition(final int x, final int y) {
            this.amountFieldPosition = new Vector2i(x, y);
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withCancelButton(final int x, final int y) {
            final Font font = Minecraft.getInstance().font;
            final int width = font.width(CANCEL_TEXT) + ACTION_BUTTON_ICON_SPACING + ICON_SIZE + 4;
            this.cancelButton = new ActionButtonPositionAndSize(
                new Vector2i(x, y),
                new Vector2i(width, ACTION_BUTTON_HEIGHT)
            );
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withResetButton(final int x, final int y) {
            final Font font = Minecraft.getInstance().font;
            final int width = font.width(RESET_TEXT) + ACTION_BUTTON_ICON_SPACING + ICON_SIZE + 4;
            this.resetButton = new ActionButtonPositionAndSize(
                new Vector2i(x, y),
                new Vector2i(width, ACTION_BUTTON_HEIGHT)
            );
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withConfirmButton(
            final Function<Integer, Vector2i> provider) {
            final Font font = Minecraft.getInstance().font;
            final int width = font.width(confirmButtonText)
                + ACTION_BUTTON_ICON_SPACING + ICON_SIZE + 4;
            this.confirmButton = new ActionButtonPositionAndSize(
                provider.apply(width),
                new Vector2i(width, ACTION_BUTTON_HEIGHT)
            );
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withVerticalActionButtons(final int x, final int y) {
            final Vector2i size = new Vector2i(VERTICAL_ACTION_BUTTONS_FIXED_WIDTH, ACTION_BUTTON_HEIGHT);
            this.cancelButton = new ActionButtonPositionAndSize(new Vector2i(x, y), size);
            this.resetButton = new ActionButtonPositionAndSize(
                new Vector2i(x, y + ACTION_BUTTON_HEIGHT + ACTION_BUTTON_VERTICAL_SPACING),
                size
            );
            this.confirmButton = new ActionButtonPositionAndSize(
                new Vector2i(x, y + ACTION_BUTTON_HEIGHT + ACTION_BUTTON_VERTICAL_SPACING
                    + ACTION_BUTTON_HEIGHT + ACTION_BUTTON_VERTICAL_SPACING),
                size
            );
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withConfirmButtonText(
            final Component newConfirmButtonText
        ) {
            this.confirmButtonText = newConfirmButtonText;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withActionButtonsEnabled(
            final boolean newActionButtonsEnabled
        ) {
            this.actionButtonsEnabled = newActionButtonsEnabled;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withMinAmount(final Supplier<T> newMinAmount) {
            this.minAmount = newMinAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withMaxAmount(final T newMaxAmount) {
            this.maxAmount = newMaxAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withResetAmount(final T newResetAmount) {
            this.resetAmount = newResetAmount;
            return this;
        }

        public AmountScreenConfiguration<T> build() {
            return new AmountScreenConfiguration<>(
                initialAmount,
                incrementsTop,
                incrementsTopStartPosition,
                incrementsBottom,
                incrementsBottomStartPosition,
                amountFieldWidth,
                amountFieldPosition,
                new ActionButtonPositions(cancelButton, resetButton, confirmButton),
                actionButtonsEnabled,
                confirmButtonText,
                minAmount,
                maxAmount,
                resetAmount
            );
        }
    }

    public record ActionButtonPositions(ActionButtonPositionAndSize cancel,
                                        ActionButtonPositionAndSize reset,
                                        ActionButtonPositionAndSize confirm) {
    }

    public record ActionButtonPositionAndSize(Vector2i pos, Vector2i size) {
        public static final ActionButtonPositionAndSize ZERO = new ActionButtonPositionAndSize(new Vector2i(0, 0),
            new Vector2i(0, 0));
    }
}
