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
        private final boolean valid;

        public ParsedValue(@Nullable final N value, final boolean valid) {
            this.value = value;
            if (value == null) {
                this.valid = false;
            } else {
                this.valid = valid;
            }
        }

        public ParsedValue(final boolean valid) {
            this.value = null;
            this.valid = valid;
        }

        public Optional<N> getValue() {
            return Optional.ofNullable(value);
        }

        public boolean isValid() {
            return valid;
        }

        public void ifValid(final Consumer<N> action) {
            if (valid) {
                action.accept(value);
            }
        }

        public void ifValidOrElse(final Consumer<N> action, final Runnable emptyAction) {
            if (valid) {
                action.accept(value);
            } else {
                emptyAction.run();
            }
        }

        public static <N extends Number> ParsedValue<N> invalid() {
            return new ParsedValue<>(false);
        }
    }
}
