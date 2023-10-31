package com.refinedmods.refinedstorage2.platform.common.support.amount;

import javax.annotation.Nullable;

import org.joml.Vector3f;

public final class AmountScreenConfiguration<T extends Number> {
    @Nullable
    private final T initialAmount;
    private final int[] incrementsTop;
    private final Vector3f incrementsTopStartPosition;
    private final int[] incrementsBottom;
    private final Vector3f incrementsBottomStartPosition;
    private final int amountFieldWidth;
    private final Vector3f amountFieldPosition;
    private final Vector3f actionButtonsStartPosition;
    private final boolean actionButtonsEnabled;
    @Nullable
    private final T minAmount;
    @Nullable
    private final T maxAmount;
    @Nullable
    private final T resetAmount;

    private AmountScreenConfiguration(@Nullable final T initialAmount,
                                      final int[] incrementsTop,
                                      final Vector3f incrementsTopStartPosition,
                                      final int[] incrementsBottom,
                                      final Vector3f incrementsBottomStartPosition,
                                      final int amountFieldWidth,
                                      final Vector3f amountFieldPosition,
                                      final Vector3f actionButtonsStartPosition,
                                      final boolean actionButtonsEnabled,
                                      @Nullable final T minAmount,
                                      @Nullable final T maxAmount,
                                      @Nullable final T resetAmount) {
        this.initialAmount = initialAmount;
        this.incrementsTop = incrementsTop;
        this.incrementsTopStartPosition = incrementsTopStartPosition;
        this.incrementsBottom = incrementsBottom;
        this.incrementsBottomStartPosition = incrementsBottomStartPosition;
        this.amountFieldWidth = amountFieldWidth;
        this.amountFieldPosition = amountFieldPosition;
        this.actionButtonsStartPosition = actionButtonsStartPosition;
        this.actionButtonsEnabled = actionButtonsEnabled;
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

    public Vector3f getIncrementsTopStartPosition() {
        return incrementsTopStartPosition;
    }

    public int[] getIncrementsBottom() {
        return incrementsBottom;
    }

    public Vector3f getIncrementsBottomStartPosition() {
        return incrementsBottomStartPosition;
    }

    public int getAmountFieldWidth() {
        return amountFieldWidth;
    }

    public Vector3f getAmountFieldPosition() {
        return amountFieldPosition;
    }

    public Vector3f getActionButtonsStartPosition() {
        return actionButtonsStartPosition;
    }

    public boolean isActionButtonsEnabled() {
        return actionButtonsEnabled;
    }

    @Nullable
    public T getMinAmount() {
        return minAmount;
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
        private Vector3f incrementsTopStartPosition = new Vector3f(7, 20, 0);
        private int[] incrementsBottom = new int[] {};
        private Vector3f incrementsBottomStartPosition = new Vector3f(7, 67, 0);
        private int amountFieldWidth = 68;
        private Vector3f amountFieldPosition = new Vector3f(0, 0, 0);
        private Vector3f actionButtonsStartPosition = new Vector3f(0, 0, 0);
        private boolean actionButtonsEnabled = true;
        @Nullable
        private T minAmount;
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

        public AmountScreenConfigurationBuilder<T> withIncrementsTopStartPosition(final Vector3f newPos) {
            this.incrementsTopStartPosition = newPos;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsBottom(final int... newIncrementsBottom) {
            this.incrementsBottom = newIncrementsBottom;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withIncrementsBottomStartPosition(final Vector3f newPos) {
            this.incrementsBottomStartPosition = newPos;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withAmountFieldWidth(final int newAmountFieldWidth) {
            this.amountFieldWidth = newAmountFieldWidth;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withAmountFieldPosition(final Vector3f newAmountFieldPosition) {
            this.amountFieldPosition = newAmountFieldPosition;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withActionButtonsStartPosition(
            final Vector3f newActionButtonsStartPosition
        ) {
            this.actionButtonsStartPosition = newActionButtonsStartPosition;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withActionButtonsEnabled(
            final boolean newActionButtonsEnabled
        ) {
            this.actionButtonsEnabled = newActionButtonsEnabled;
            return this;
        }

        public AmountScreenConfigurationBuilder<T> withMinAmount(final T newMinAmount) {
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
                actionButtonsStartPosition,
                actionButtonsEnabled,
                minAmount,
                maxAmount,
                resetAmount
            );
        }
    }
}
