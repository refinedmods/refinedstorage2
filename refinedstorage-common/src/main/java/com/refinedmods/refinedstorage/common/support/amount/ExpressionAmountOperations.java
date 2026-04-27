package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.query.evaluator.Evaluator;
import com.refinedmods.refinedstorage.query.evaluator.EvaluatorException;
import com.refinedmods.refinedstorage.query.lexer.Source;

import java.text.DecimalFormat;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.util.Mth;


public class ExpressionAmountOperations implements AmountOperations<Double> {
    public static final AmountOperations<Double> INSTANCE = new ExpressionAmountOperations();

    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.###");

    private static final String SOURCE = "Amount screen input";

    @Override
    public String format(final Double value) {
        return DECIMAL_FORMATTER.format(value);
    }

    @Override
    public ParsedValue<Double> parse(final String value) {
        try {
            final Evaluator calculator = new Evaluator(new Source(SOURCE, value));
            return new ParsedValue<>(calculator.evaluate());
        } catch (final EvaluatorException e) {
            return ParsedValue.invalid();
        }
    }

    @Override
    public Double changeAmount(final @Nullable Double current,
                               final int delta,
                               @Nullable final Double minAmount,
                               @Nullable final Double maxAmount) {
        if (current == null) {
            return Mth.clamp(
                    delta,
                    Objects.requireNonNullElse(minAmount, Double.MIN_VALUE),
                    Objects.requireNonNullElse(maxAmount, Double.MAX_VALUE)
            );
        }
        return Mth.clamp(
            current + delta,
            Objects.requireNonNullElse(minAmount, Double.MIN_VALUE),
            Objects.requireNonNullElse(maxAmount, Double.MAX_VALUE)
        );
    }
}
