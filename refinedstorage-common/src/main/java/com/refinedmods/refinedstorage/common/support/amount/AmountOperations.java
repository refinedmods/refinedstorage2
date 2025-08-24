package com.refinedmods.refinedstorage.common.support.amount;

import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

public interface AmountOperations<N extends Number> {
    String format(N value);

    ParsedValue<N> parse(String value);

    N changeAmount(@Nullable N current, int delta, @Nullable N minAmount, @Nullable N maxAmount);

    class ParsedValue<N extends Number> {
        private final @Nullable N value;
        private boolean outOfBounds;

        public ParsedValue(@Nullable final N value) {
            this.value = value;
            this.outOfBounds = false;
        }

        public Optional<N> getValue() {
            return Optional.ofNullable(value);
        }

        public boolean isValid() {
            return !outOfBounds && value != null;
        }

        public void ifValid(final Consumer<N> action) {
            if (isValid()) {
                action.accept(value);
            }
        }

        public void ifValidOrElse(final Consumer<N> action, final Runnable emptyAction) {
            if (isValid()) {
                action.accept(value);
            } else {
                emptyAction.run();
            }
        }

        public void setOutOfBounds() {
            outOfBounds = true;
        }

        public static <N extends Number> ParsedValue<N> invalid() {
            return new ParsedValue<>(null);
        }
    }
}
