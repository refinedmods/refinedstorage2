package com.refinedmods.refinedstorage.common.support.amount;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

public interface AmountOperations<N extends Number> {
    String format(N value);

    Optional<N> parse(String value);

    Optional<N> validate(N amount, @Nullable N minAmount, @Nullable N maxAmount);

    N changeAmount(@Nullable N current, int delta, @Nullable N minAmount, @Nullable N maxAmount);
}
