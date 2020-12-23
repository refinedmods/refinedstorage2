package com.refinedmods.refinedstorage2.core.query.parser;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.query.lexer.TokenType;
import com.refinedmods.refinedstorage2.core.query.parser.node.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class ParserTest {
    private ParserBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ParserBuilder();
    }

    @Test
    void Test_identifier_literal() {
        // Act
        List<Node> nodes = builder.token("hello", TokenType.IDENTIFIER).getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("hello");
    }

    @Test
    void Test_number_literal() {
        // Act
        List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("2.345", TokenType.FLOATING_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0)).hasToString("1");
        assertThat(nodes.get(1)).hasToString("2.345");
    }

    @Test
    void Test_simple_binary_operator() {
        // Act
        List<Node> nodes = builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP)
            .token("2", TokenType.INTEGER_NUMBER)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(1 + 2)");
    }

    @Test
    void Test_left_to_right_binary_operator_associativity() {
        // Act
        List<Node> nodes = builder
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
    void Test_right_to_left_binary_operator_associativity() {
        // Act
        List<Node> nodes = builder
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
    void Test_binary_operator_associativity() {
        // Act
        List<Node> nodes = builder
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
    void Test_binary_operator_precedence() {
        // Act
        List<Node> nodes = builder
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
    void Test_logical_operator_precedence() {
        // Act
        List<Node> nodes = builder
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
    void Test_prefixed_unary_operator() {
        // Act
        List<Node> nodes = builder
            .token("!", TokenType.UNARY_OP)
            .token("true", TokenType.IDENTIFIER)
            .token("++", TokenType.UNARY_OP)
            .token("x", TokenType.IDENTIFIER)
            .token("--", TokenType.UNARY_OP)
            .token("!", TokenType.UNARY_OP)
            .token("(", TokenType.PAREN_OPEN)
            .token("x", TokenType.IDENTIFIER)
            .token("&&", TokenType.BIN_OP)
            .token("y", TokenType.IDENTIFIER)
            .token(")", TokenType.PAREN_CLOSE)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(3);
        assertThat(nodes.get(0)).hasToString("!true++");
        assertThat(nodes.get(1)).hasToString("x--");
        assertThat(nodes.get(2)).hasToString("!((x && y))");
    }

    @Test
    void Test_prefix_unary_operator_with_no_target() {
        // Arrange
        builder.token("!", TokenType.UNARY_OP);

        // Act
        ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unary operator has no target");
        assertThat(e.getToken().getContent()).isEqualTo("!");
    }

    @Test
    void Test_invalid_suffixed_unary_operator() {
        // Act
        builder
            .token("true", TokenType.IDENTIFIER)
            .token("!", TokenType.UNARY_OP);

        // Assert
        ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Cannot use '!' as suffixed unary operator");
        assertThat(e.getToken().getContent()).isEqualTo("!");
    }

    @Test
    void Test_suffixed_unary_operator() {
        // Act
        List<Node> nodes = builder
            .token("x", TokenType.IDENTIFIER)
            .token("++", TokenType.UNARY_OP)
            .token("y", TokenType.IDENTIFIER)
            .token("--", TokenType.UNARY_OP)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0)).hasToString("x++");
        assertThat(nodes.get(1)).hasToString("y--");
    }

    @Test
    void Test_parenthesis() {
        // Act
        List<Node> nodes = builder
            .token("(", TokenType.PAREN_OPEN)
            .token("1", TokenType.INTEGER_NUMBER)
            .token(")", TokenType.PAREN_CLOSE)
            .getNodes();

        // Assert
        assertThat(nodes).hasSize(1);
        assertThat(nodes.get(0)).hasToString("(1)");
    }

    @Test
    void Test_nested_parenthesis() {
        // Act
        List<Node> nodes = builder
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
    void Test_unbalanced_parenthesis() {
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
        ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Expected ')'");
        assertThat(e.getToken().getContent()).isEqualTo(")");
    }

    @Test
    void Test_unclosed_parenthesis() {
        // Act
        builder.token("(", TokenType.PAREN_OPEN);

        // Assert
        ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Unclosed parenthesis");
        assertThat(e.getToken().getContent()).isEqualTo("(");
    }

    @Test
    void Test_unfinished_binary_operator() {
        // Act
        builder
            .token("1", TokenType.INTEGER_NUMBER)
            .token("+", TokenType.BIN_OP);

        // Assert
        ParserException e = assertThrows(ParserException.class, () -> builder.getNodes());
        assertThat(e.getMessage()).isEqualTo("Unfinished binary operator expression");
        assertThat(e.getToken().getContent()).isEqualTo("+");
    }
}
