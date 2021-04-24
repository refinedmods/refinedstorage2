package com.refinedmods.refinedstorage2.core.query.lexer;

import com.refinedmods.refinedstorage2.core.Rs2Test;

import java.util.List;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.query.lexer.TokenAssertions.assertPosition;
import static com.refinedmods.refinedstorage2.core.query.lexer.TokenAssertions.assertRange;
import static com.refinedmods.refinedstorage2.core.query.lexer.TokenAssertions.assertToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class LexerTest {
    private static final LexerTokenMappings TEST_TOKEN_MAPPINGS = new LexerTokenMappings()
            .addMapping(new LexerTokenMapping("&&", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("(", TokenType.PAREN_OPEN))
            .addMapping(new LexerTokenMapping(")", TokenType.PAREN_CLOSE))
            .addMapping(new LexerTokenMapping("+", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("-", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("||", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("*", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("/", TokenType.BIN_OP))
            .addMapping(new LexerTokenMapping("!", TokenType.UNARY_OP));

    private static final String SOURCE_NAME = "<test>";

    @Test
    void Test_invalid_character() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("$hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unexpected '$'");
        assertRange(e.getRange(), 1, 1, 1, 1);
    }

    @Test
    void Test_single_identifier() {
        // Act
        List<Token> tokens = getTokens("hel1lo");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        assertToken(token, "hel1lo", TokenType.IDENTIFIER);
        assertPosition(token.getPosition(), SOURCE_NAME, 1, 1, 1, 6);
    }

    @Test
    void Test_multiple_identifiers() {
        // Act
        List<Token> tokens = getTokens("hello wo1rld baz");

        // Assert
        assertThat(tokens).hasSize(3);

        Token hello = tokens.get(0);
        assertToken(hello, "hello", TokenType.IDENTIFIER);
        assertPosition(hello.getPosition(), SOURCE_NAME, 1, 1, 1, 5);

        Token world = tokens.get(1);
        assertToken(world, "wo1rld", TokenType.IDENTIFIER);
        assertPosition(world.getPosition(), SOURCE_NAME, 1, 7, 1, 12);

        Token baz = tokens.get(2);
        assertToken(baz, "baz", TokenType.IDENTIFIER);
        assertPosition(baz.getPosition(), SOURCE_NAME, 1, 14, 1, 16);
    }

    @Test
    void Test_single_string_identifier() {
        // Act
        List<Token> tokens = getTokens("\"h_el1lo\"");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        assertToken(token, "h_el1lo", TokenType.IDENTIFIER);
        assertPosition(token.getPosition(), SOURCE_NAME, 1, 1, 1, 9);
    }

    @Test
    void Test_multiple_string_identifiers() {
        // Act
        List<Token> tokens = getTokens("\"hello\" \"_World\" \"baz\"");

        // Assert
        assertThat(tokens).hasSize(3);

        Token hello = tokens.get(0);
        assertToken(hello, "hello", TokenType.IDENTIFIER);
        assertPosition(hello.getPosition(), SOURCE_NAME, 1, 1, 1, 7);

        Token world = tokens.get(1);
        assertToken(world, "_World", TokenType.IDENTIFIER);
        assertPosition(world.getPosition(), SOURCE_NAME, 1, 9, 1, 16);

        Token baz = tokens.get(2);
        assertToken(baz, "baz", TokenType.IDENTIFIER);
        assertPosition(baz.getPosition(), SOURCE_NAME, 1, 18, 1, 22);
    }

    @Test
    void Test_empty_string_identifier() {
        // Act
        List<Token> tokens = getTokens("\"\"");

        // Assert
        assertThat(tokens).hasSize(1);

        Token text = tokens.get(0);
        assertToken(text, "", TokenType.IDENTIFIER);
        assertPosition(text.getPosition(), SOURCE_NAME, 1, 1, 1, 2);
    }

    @Test
    void Test_unfinished_string_identifier() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("\"hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unexpected end of string");
        assertRange(e.getRange(), 1, 1, 1, 6);
    }

    @Test
    void Test_new_lines() {
        // Act
        List<Token> tokens = getTokens("hello world\r\r\nbaz\n\n123");

        // Assert
        assertThat(tokens).hasSize(4);

        Token hello = tokens.get(0);
        assertToken(hello, "hello", TokenType.IDENTIFIER);
        assertPosition(hello.getPosition(), SOURCE_NAME, 1, 1, 1, 5);

        Token world = tokens.get(1);
        assertToken(world, "world", TokenType.IDENTIFIER);
        assertPosition(world.getPosition(), SOURCE_NAME, 1, 7, 1, 11);

        Token baz = tokens.get(2);
        assertToken(baz, "baz", TokenType.IDENTIFIER);
        assertPosition(baz.getPosition(), SOURCE_NAME, 2, 1, 2, 3);

        Token number = tokens.get(3);
        assertToken(number, "123", TokenType.INTEGER_NUMBER);
        assertPosition(number.getPosition(), SOURCE_NAME, 4, 1, 4, 3);
    }

    @Test
    void Test_integer_number() {
        // Act
        List<Token> tokens = getTokens("123");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        assertToken(token, "123", TokenType.INTEGER_NUMBER);
        assertPosition(token.getPosition(), SOURCE_NAME, 1, 1, 1, 3);
    }

    @Test
    void Test_floating_number() {
        // Act
        List<Token> tokens = getTokens("123.45");

        // Assert
        assertThat(tokens).hasSize(1);

        Token token = tokens.get(0);
        assertToken(token, "123.45", TokenType.FLOATING_NUMBER);
        assertPosition(token.getPosition(), SOURCE_NAME, 1, 1, 1, 6);
    }

    @Test
    void Test_floating_number_with_digits_after_point() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("123."));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Unexpected end of number");
        assertRange(e.getRange(), 1, 1, 1, 4);
    }

    @Test
    void Test_floating_number_without_digits_after_point() {
        // Act
        LexerException e = assertThrows(LexerException.class, () -> getTokens("123.abc"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Invalid floating point number");
        assertRange(e.getRange(), 1, 1, 1, 4);
    }

    @Test
    void Test_token_mappings() {
        // Arrange
        Lexer lexer = createLexer("()+-/*!&&||");

        // Act
        lexer.scan();

        // Assert
        List<Token> tokens = lexer.getTokens();

        assertThat(tokens).hasSize(9);

        assertToken(tokens.get(0), "(", TokenType.PAREN_OPEN);
        assertToken(tokens.get(1), ")", TokenType.PAREN_CLOSE);
        assertToken(tokens.get(2), "+", TokenType.BIN_OP);
        assertToken(tokens.get(3), "-", TokenType.BIN_OP);
        assertToken(tokens.get(4), "/", TokenType.BIN_OP);
        assertToken(tokens.get(5), "*", TokenType.BIN_OP);
        assertToken(tokens.get(6), "!", TokenType.UNARY_OP);
        assertToken(tokens.get(7), "&&", TokenType.BIN_OP);
        assertToken(tokens.get(8), "||", TokenType.BIN_OP);

        assertPosition(tokens.get(0).getPosition(), SOURCE_NAME, 1, 1, 1, 1);
        assertPosition(tokens.get(1).getPosition(), SOURCE_NAME, 1, 2, 1, 2);
        assertPosition(tokens.get(2).getPosition(), SOURCE_NAME, 1, 3, 1, 3);
        assertPosition(tokens.get(3).getPosition(), SOURCE_NAME, 1, 4, 1, 4);
        assertPosition(tokens.get(4).getPosition(), SOURCE_NAME, 1, 5, 1, 5);
        assertPosition(tokens.get(5).getPosition(), SOURCE_NAME, 1, 6, 1, 6);
        assertPosition(tokens.get(6).getPosition(), SOURCE_NAME, 1, 7, 1, 7);
        assertPosition(tokens.get(7).getPosition(), SOURCE_NAME, 1, 8, 1, 9);
        assertPosition(tokens.get(8).getPosition(), SOURCE_NAME, 1, 10, 1, 11);
    }

    private Lexer createLexer(String content) {
        return new Lexer(new Source(SOURCE_NAME, content), TEST_TOKEN_MAPPINGS);
    }

    private List<Token> getTokens(String content) {
        Lexer lexer = createLexer(content);
        lexer.scan();
        return lexer.getTokens();
    }
}
