package com.refinedmods.refinedstorage2.core.query.lexer;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class LexerTest {
    private static final String SOURCE_NAME = "<test>";

    @Test
    void Test_single_identifier() {
        // Act
        List<Token> tokens = getTokens("$h_el1lo");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        verifyToken(token, "$h_el1lo", TokenType.IDENTIFIER);
        verifyPosition(token.getPosition(), 1, 1, 1, 8);
    }

    @Test
    void Test_multiple_identifiers() {
        // Act
        List<Token> tokens = getTokens("hello _World baz");

        // Assert
        assertThat(tokens).hasSize(3);

        Token hello = tokens.get(0);
        verifyToken(hello, "hello", TokenType.IDENTIFIER);
        verifyPosition(hello.getPosition(), 1, 1, 1, 5);

        Token world = tokens.get(1);
        verifyToken(world, "_World", TokenType.IDENTIFIER);
        verifyPosition(world.getPosition(), 1, 7, 1, 12);

        Token baz = tokens.get(2);
        verifyToken(baz, "baz", TokenType.IDENTIFIER);
        verifyPosition(baz.getPosition(), 1, 14, 1, 16);
    }

    @Test
    void Test_single_string_identifier() {
        // Act
        List<Token> tokens = getTokens("\"$h_el1lo\"");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        verifyToken(token, "$h_el1lo", TokenType.IDENTIFIER);
        verifyPosition(token.getPosition(), 1, 1, 1, 10);
    }

    @Test
    void Test_multiple_string_identifiers() {
        // Act
        List<Token> tokens = getTokens("\"hello\" \"_World\" \"baz\"");

        // Assert
        assertThat(tokens).hasSize(3);

        Token hello = tokens.get(0);
        verifyToken(hello, "hello", TokenType.IDENTIFIER);
        verifyPosition(hello.getPosition(), 1, 1, 1, 7);

        Token world = tokens.get(1);
        verifyToken(world, "_World", TokenType.IDENTIFIER);
        verifyPosition(world.getPosition(), 1, 9, 1, 16);

        Token baz = tokens.get(2);
        verifyToken(baz, "baz", TokenType.IDENTIFIER);
        verifyPosition(baz.getPosition(), 1, 18, 1, 22);
    }

    @Test
    void Test_empty_string() {
        // Act
        List<Token> tokens = getTokens("\"\"");

        // Assert
        assertThat(tokens).hasSize(1);

        Token text = tokens.get(0);
        verifyToken(text, "", TokenType.IDENTIFIER);
        verifyPosition(text.getPosition(), 1, 1, 1, 2);
    }

    @Test
    void Test_unfinished_string_identifier() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("\"hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unexpected end of string");
        verifyRange(e.getRange(), 1, 1, 1, 6);
    }

    @Test
    void Test_new_lines() {
        // Act
        List<Token> tokens = getTokens("hello _World\r\r\nbaz\n\n123");

        // Assert
        assertThat(tokens).hasSize(4);

        Token hello = tokens.get(0);
        verifyToken(hello, "hello", TokenType.IDENTIFIER);
        verifyPosition(hello.getPosition(), 1, 1, 1, 5);

        Token world = tokens.get(1);
        verifyToken(world, "_World", TokenType.IDENTIFIER);
        verifyPosition(world.getPosition(), 1, 7, 1, 12);

        Token baz = tokens.get(2);
        verifyToken(baz, "baz", TokenType.IDENTIFIER);
        verifyPosition(baz.getPosition(), 2, 1, 2, 3);

        Token number = tokens.get(3);
        verifyToken(number, "123", TokenType.INTEGER_NUMBER);
        verifyPosition(number.getPosition(), 4, 1, 4, 3);
    }

    @Test
    void Test_integer_number() {
        // Act
        List<Token> tokens = getTokens("123");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        verifyToken(token, "123", TokenType.INTEGER_NUMBER);
        verifyPosition(token.getPosition(), 1, 1, 1, 3);
    }

    @Test
    void Test_floating_number() {
        // Act
        List<Token> tokens = getTokens("123.45");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        verifyToken(token, "123.45", TokenType.FLOATING_NUMBER);
        verifyPosition(token.getPosition(), 1, 1, 1, 6);
    }

    @Test
    void Test_floating_number_with_digits_after_point() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("123."));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unexpected end of number");
        verifyRange(e.getRange(), 1, 1, 1, 4);
    }

    @Test
    void Test_floating_number_without_digits_after_point() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("123.abc"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Invalid floating point number");
        verifyRange(e.getRange(), 1, 1, 1, 4);
    }

    @Test
    void Test_fixed_tokens() {
        // Act
        List<Token> tokens = getTokens("()+-/*!");

        // Assert
        assertThat(tokens).hasSize(7);

        verifyToken(tokens.get(0), "(", TokenType.PAREN_OPEN);
        verifyToken(tokens.get(1), ")", TokenType.PAREN_CLOSE);
        verifyToken(tokens.get(2), "+", TokenType.BIN_OP);
        verifyToken(tokens.get(3), "-", TokenType.BIN_OP);
        verifyToken(tokens.get(4), "/", TokenType.BIN_OP);
        verifyToken(tokens.get(5), "*", TokenType.BIN_OP);
        verifyToken(tokens.get(6), "!", TokenType.UNARY_OP);

        verifyPosition(tokens.get(0).getPosition(), 1, 1, 1, 1);
        verifyPosition(tokens.get(1).getPosition(), 1, 2, 1, 2);
        verifyPosition(tokens.get(2).getPosition(), 1, 3, 1, 3);
        verifyPosition(tokens.get(3).getPosition(), 1, 4, 1, 4);
        verifyPosition(tokens.get(4).getPosition(), 1, 5, 1, 5);
        verifyPosition(tokens.get(5).getPosition(), 1, 6, 1, 6);
        verifyPosition(tokens.get(6).getPosition(), 1, 7, 1, 7);
    }

    private List<Token> getTokens(String content) {
        Lexer lexer = new Lexer(new Source(SOURCE_NAME, content));
        lexer.scan();
        return lexer.getTokens();
    }

    private void verifyToken(Token token, String content, TokenType type) {
        assertThat(token.getContent()).isEqualTo(content);
        assertThat(token.getType()).isEqualTo(type);
    }

    private void verifyPosition(TokenPosition position, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(position.getSource().getName()).isEqualTo(SOURCE_NAME);
        verifyRange(position.getRange(), startLine, startColumn, endLine, endColumn);
    }

    private void verifyRange(TokenRange range, int startLine, int startColumn, int endLine, int endColumn) {
        assertThat(range.getStartLine()).isEqualTo(startLine);
        assertThat(range.getStartColumn()).isEqualTo(startColumn);
        assertThat(range.getEndLine()).isEqualTo(endLine);
        assertThat(range.getEndColumn()).isEqualTo(endColumn);
    }
}
