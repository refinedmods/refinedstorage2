package com.refinedmods.refinedstorage.query.evaluator;

import com.refinedmods.refinedstorage.query.lexer.Source;

import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorTest {

    private static final String SOURCE_NAME = "<test>";
    final DecimalFormat df = new DecimalFormat("#.###");

    private String buildAndExecute(final String expression) {
        try {
            final Evaluator math = new Evaluator(new Source(SOURCE_NAME, expression));
            return df.format(math.evaluate());
        } catch (EvaluatorException e) {
            return e.getMessage();
        }
    }

    @Test
    void testBasicOperations() {
        assertEquals("2", buildAndExecute("1 + 1"));
        assertEquals("1", buildAndExecute("2 - 1"));
        assertEquals("12", buildAndExecute("3 * 4"));
        assertEquals("4", buildAndExecute("8 / 2"));
    }

    @Test
    void testOperatorPrecedence() {
        assertEquals("14", buildAndExecute("2 + 3 * 4"));
        assertEquals("7", buildAndExecute("10 - 6 / 2"));
        assertEquals("25", buildAndExecute("10 / 2 * 5"));
        assertEquals("24", buildAndExecute("5 + 2 * 10 - 3 / 3"));
    }

    @Test
    void testParentheses() {
        assertEquals("20", buildAndExecute("(2 + 3) * 4"));
        assertEquals("2", buildAndExecute("10 / (2 + 3)"));
        assertEquals("8", buildAndExecute("(3 + 5) * (2 - 1)"));
        assertEquals("6", buildAndExecute("((10 - 2) * 3) / 4"));
    }

    @Test
    void testDecimals() {
        assertEquals("3", buildAndExecute("2.5 + 0.5"));
        assertEquals("6.72", buildAndExecute("3.2 * 2.1"));
        assertEquals("3.3", buildAndExecute("5.5 - 2.2"));
        assertEquals("4", buildAndExecute("8.8 / 2.2"));
    }

    @Test
    void testLargeNumbers() {
        assertEquals("3000000", buildAndExecute("1m + 2m"));
        assertEquals("1999999998", buildAndExecute("(1g-1) * 2"));
        assertEquals("500000000", buildAndExecute("1g / 2"));
        assertEquals("1000000000", buildAndExecute("(1g + 1g) / 2"));
    }

    @Test
    void testEdgeCases() {
        assertEquals("0", buildAndExecute("0 + 0"));
        assertEquals("0", buildAndExecute("0 * 100"));
        assertEquals("100", buildAndExecute("100 / 1"));
        assertEquals("0.01", buildAndExecute("1 / 100"));
    }

    @Test
    void testInvalidInputs() {
        assertEquals("resource_amount_input.syntax_error", buildAndExecute("2 +"));
        assertEquals("resource_amount_input.syntax_error", buildAndExecute("* 3"));
        assertEquals("∞", buildAndExecute("10 / 0"));
        assertEquals("resource_amount_input.syntax_error", buildAndExecute("(3 + 2"));
        assertEquals("resource_amount_input.no_value_before_unit", buildAndExecute("abc + 1"));
        assertEquals("resource_amount_input.invalid_unit", buildAndExecute("1d"));
        assertEquals("resource_amount_input.no_value_before_unit", buildAndExecute("k"));
        assertEquals("resource_amount_input.unrecognized_char", buildAndExecute("1$2"));
    }
}
