package com.refinedmods.refinedstorage.common.support.amount;

import java.util.Optional;
import javax.annotation.Nullable;

public interface AmountOperations<N extends Number> {
    String format(N value);

    ReturnValue<N> parse(String value);

    default ReturnValue<N> calculate(final String amount) {
        final ReturnValue<N> evaluation = parse(amount);
        if (evaluation.value == null) {
            return new ReturnValue<>(evaluation.tooltip);
        }
        return evaluation;
    }

    N changeAmount(N current, int delta, @Nullable N minAmount, @Nullable N maxAmount);

    class ReturnValue<N extends Number> {
        private final @Nullable N value;
        private final String tooltip;

        public ReturnValue(final String error) {
            value = null;
            tooltip = error;
        }

        public ReturnValue(@Nullable final N value, final String tooltip) {
            this.value = value;
            this.tooltip = tooltip;
        }

        public Optional<N> getValue() {
            return Optional.ofNullable(value);
        }

        public String getTooltip() {
            return tooltip;
        }
    }
}
