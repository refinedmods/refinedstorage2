package com.refinedmods.refinedstorage2.query.parser;

import com.refinedmods.refinedstorage2.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.query.parser.node.Node;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {
    private ParserBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ParserBuilder();
    }

    @Test
    void testIdentifierLiteral() {
        // Act
        final List<Node> nodes = builder.token("hello", TokenType.IDENTIFIER).getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("hello");
    }

    @Test
    void testNumberLiteral() {
        // Act
        final List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("2.345", TokenType.FLOATING_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0)).hasToString("1");
        assertThat(nodes.get(1)).hasToString("2.345");
    }

    @Test
    void testSimpleBinaryOperator() {
        // Act
        final List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(1 + 2)");
    }

    @Test
    void testLeftToRightBinaryOperatorAssociativity() {
        // Act
        final List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("3", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(((1 + 2) + 3) + 4)");
    }

    @Test
    void testRightToLeftBinaryOperatorAssociativity() {
        // Act
        final List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("3", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(1 ^ (2 ^ (3 ^ 4)))");
    }

    @Test
    void testBinaryOperatorAssociativity() {
        // Act
        final List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("3", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .token("*", TokenType.BIN_OP)
            .token("5", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("6", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("7", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(((1 + 2) + 3) + (4 * (5 ^ (6 ^ 7))))");
    }

    @Test
    void testBinaryOperatorPrecedence() {
        // Act
        final List<Node> nodes = builder
            .token("x", TokenType.IDENTIFIER)
            .token("=", TokenType.BIN_OP)
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("*", TokenType.BIN_OP)
            .token("3", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(x = ((1 + (2 * 3)) + 4))");
    }

    @Test
    void testLogicalOperatorPrecedence() {
        // Act
        final List<Node> nodes = builder
            .token("x", TokenType.IDENTIFIER)
            .token("&&", TokenType.BIN_OP)
            .token("y", TokenType.IDENTIFIER)
            .token("||", TokenType.BIN_OP)
            .token("z", TokenType.IDENTIFIER)
            .token("&&", TokenType.BIN_OP)
            .token("a", TokenType.IDENTIFIER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("((x && y) || (z && a))");
    }

    @Test
    void testUnaryOperators() {
        // Act
        final List<Node> nodes = builder
            .token("!", TokenType.UNARY_OP)
            .token("true", TokenType.IDENTIFIER)
            .token("++", TokenType.UNARY_OP)
            .token("x", TokenType.IDENTIFIER)
            .token("!", TokenType.UNARY_OP)
            .token("(", TokenType.PAREN_OPEN)
            .token("x", TokenType.IDENTIFIER)
            .token("&&", TokenType.BIN_OP)
            .token("y", TokenType.IDENTIFIER)
            .token(")", TokenType.PAREN_CLOSE)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(3);
        assertThat(nodes.get(0)).hasToString("!true");
        assertThat(nodes.get(1)).hasToString("++x");
        assertThat(nodes.get(2)).hasToString("!((x && y))");
    }

    @Test
    void testUnaryOperatorWithNoTarget() {
        // Arrange
        builder.token("!", TokenType.UNARY_OP);

        // Act
        final ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unary operator has no target");
        assertThat(e.getToken().content()).isEqualTo("!");
    }

    @Test
    void testParenthesis() {
        // Act
        final List<Node> nodes = builder
            .token("(", TokenType.PAREN_OPEN)
            .token("1", TokenType.INTEGER_NUMBER)
            .token(")", TokenType.PAREN_CLOSE)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(1)");
    }

    @Test
    void testNestedParenthesis() {
        // Act
        final List<Node> nodes = builder
            .token("(", TokenType.PAREN_OPEN)
            .token("(", TokenType.PAREN_OPEN)
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("1", TokenType.INTEGER_NUMBER)
            .token("*", TokenType.BIN_OP)
            .token("(", TokenType.PAREN_OPEN)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .token(")", TokenType.PAREN_CLOSE)
            .token(")", TokenType.PAREN_CLOSE)
            .token(")", TokenType.PAREN_CLOSE)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(((1 + (1 * ((2 ^ 4))))))");
    }

    @Test
    void testUnbalancedParenthesis() {
        // Act
        builder
            .token("(", TokenType.PAREN_OPEN)
            .token("(", TokenType.PAREN_OPEN)
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("1", TokenType.INTEGER_NUMBER)
            .token("*", TokenType.BIN_OP)
            .token("(", TokenType.PAREN_OPEN)
            .token("2", TokenType.INTEGER_NUMBER)
            .token("^", TokenType.BIN_OP)
            .token("4", TokenType.INTEGER_NUMBER)
            .token(")", TokenType.PAREN_CLOSE)
            .token(")", TokenType.PAREN_CLOSE);

        // Assert
        final ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Expected ')'");
        assertThat(e.getToken().content()).isEqualTo(")");
    }

    @Test
    void testUnclosedParenthesis() {
        // Act
        builder.token("(", TokenType.PAREN_OPEN);

        // Assert
        final ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Unclosed parenthesis");
        assertThat(e.getToken().content()).isEqualTo("(");
    }

    @Test
    void testMultipleExpressionsInParenthesis() {
        // Act
        builder.token("(", TokenType.PAREN_OPEN);
        builder.token("a", TokenType.IDENTIFIER);
        builder.token("b", TokenType.IDENTIFIER);
        builder.token(")", TokenType.PAREN_CLOSE);

        // Assert
        final List<Node> nodes = builder.getNodes();
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(a b)");
    }

    @Test
    void testUnfinishedBinaryOperatorExpression() {
        // Act
        builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP);

        // Assert
        final ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Unfinished binary operator expression");
        assertThat(e.getToken().content()).isEqualTo("+");
    }
}
