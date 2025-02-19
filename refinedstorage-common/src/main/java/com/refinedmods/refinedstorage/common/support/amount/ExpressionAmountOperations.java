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
    public String format(@Nullable final Double value) {
        return (value == null) ? "" : DECIMAL_FORMATTER.format(value);
    }

    @Override
    public ReturnValue<Double> parse(final String value) {
        Double result = null;
        String tooltip;
        try {
            final Evaluator calculator = new Evaluator(new Source(SOURCE, value));
            result = calculator.evaluate();
            tooltip = "=" + format(result);
        } catch (EvaluatorException e) {
            tooltip = e.getMessage();
        }
        return new ReturnValue<>(result, tooltip);
    }

    @Override
    public Double changeAmount(final @Nullable Double current,
                               final int delta,
                               @Nullable final Double minAmount,
                               @Nullable final Double maxAmount) {
        if (current == null) {
            return (double) delta;
        }
        return Mth.clamp(
            current + delta,
            Objects.requireNonNullElse(minAmount, Double.MIN_VALUE),
            Objects.requireNonNullElse(maxAmount, Double.MAX_VALUE)
        );
    }
}
