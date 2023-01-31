package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import org.joml.Vector3f;

public final class AmountScreenConfiguration {
    private final int initialAmount;
    private final int[] incrementsTop;
    private final int[] incrementsBottom;
    private final Vector3f amountFieldPosition;
    private final Vector3f actionButtonsStartPosition;
    private final int minAmount;
    private final int maxAmount;
    private final int resetAmount;

    private AmountScreenConfiguration(final int initialAmount,
                                      final int[] incrementsTop,
                                      final int[] incrementsBottom,
                                      final Vector3f amountFieldPosition,
                                      final Vector3f actionButtonsStartPosition,
                                      final int minAmount,
                                      final int maxAmount,
                                      final int resetAmount) {
        this.initialAmount = initialAmount;
        this.incrementsTop = incrementsTop;
        this.incrementsBottom = incrementsBottom;
        this.amountFieldPosition = amountFieldPosition;
        this.actionButtonsStartPosition = actionButtonsStartPosition;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.resetAmount = resetAmount;
    }

    public int getInitialAmount() {
        return initialAmount;
    }

    public int[] getIncrementsTop() {
        return incrementsTop;
    }

    public int[] getIncrementsBottom() {
        return incrementsBottom;
    }

    public Vector3f getAmountFieldPosition() {
        return amountFieldPosition;
    }

    public Vector3f getActionButtonsStartPosition() {
        return actionButtonsStartPosition;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getResetAmount() {
        return resetAmount;
    }

    public static final class AmountScreenConfigurationBuilder {
        private int initialAmount;
        private int[] incrementsTop = new int[] {};
        private int[] incrementsBottom = new int[] {};
        private Vector3f amountFieldPosition = new Vector3f(0, 0, 0);
        private Vector3f actionButtonsStartPosition = new Vector3f(0, 0, 0);
        private int minAmount;
        private int maxAmount;
        private int resetAmount;

        private AmountScreenConfigurationBuilder() {
        }

        public static AmountScreenConfigurationBuilder create() {
            return new AmountScreenConfigurationBuilder();
        }

        public AmountScreenConfigurationBuilder withInitialAmount(final int newInitialAmount) {
            this.initialAmount = newInitialAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder withIncrementsTop(final int... newIncrementsTop) {
            this.incrementsTop = newIncrementsTop;
            return this;
        }

        public AmountScreenConfigurationBuilder withIncrementsBottom(final int... newIncrementsBottom) {
            this.incrementsBottom = newIncrementsBottom;
            return this;
        }

        public AmountScreenConfigurationBuilder withAmountFieldPosition(final Vector3f newAmountFieldPosition) {
            this.amountFieldPosition = newAmountFieldPosition;
            return this;
        }

        public AmountScreenConfigurationBuilder withActionButtonsStartPosition(
            final Vector3f newActionButtonsStartPosition
        ) {
            this.actionButtonsStartPosition = newActionButtonsStartPosition;
            return this;
        }

        public AmountScreenConfigurationBuilder withMinAmount(final int newMinAmount) {
            this.minAmount = newMinAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder withMaxAmount(final int newMaxAmount) {
            this.maxAmount = newMaxAmount;
            return this;
        }

        public AmountScreenConfigurationBuilder withResetAmount(final int newResetAmount) {
            this.resetAmount = newResetAmount;
            return this;
        }

        public AmountScreenConfiguration build() {
            return new AmountScreenConfiguration(
                initialAmount,
                incrementsTop,
                incrementsBottom,
                amountFieldPosition,
                actionButtonsStartPosition,
                minAmount,
                maxAmount,
                resetAmount
            );
        }
    }
}
