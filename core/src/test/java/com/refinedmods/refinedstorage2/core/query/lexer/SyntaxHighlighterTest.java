package com.refinedmods.refinedstorage2.core.query.lexer;

import com.refinedmods.refinedstorage2.core.Rs2Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class SyntaxHighlighterTest {
    private static Stream<Arguments> provideInput() {
        return Stream.of(
                Arguments.of("", Collections.emptyList()),
                Arguments.of(
                        "    ",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE")
                        )
                ),
                Arguments.of(
                        "    !e ",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE"),
                                new SyntaxHighlightedCharacter("!", "AQUA"),
                                new SyntaxHighlightedCharacter("e", "WHITE"),
                                new SyntaxHighlightedCharacter(" ", "WHITE")
                        )
                ),
                Arguments.of(
                        "hello",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter("h", "WHITE"),
                                new SyntaxHighlightedCharacter("e", "WHITE"),
                                new SyntaxHighlightedCharacter("l", "WHITE"),
                                new SyntaxHighlightedCharacter("l", "WHITE"),
                                new SyntaxHighlightedCharacter("o", "WHITE")
                        )
                ),
                Arguments.of(
                        "!not&&",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter("!", "AQUA"),
                                new SyntaxHighlightedCharacter("n", "WHITE"),
                                new SyntaxHighlightedCharacter("o", "WHITE"),
                                new SyntaxHighlightedCharacter("t", "WHITE"),
                                new SyntaxHighlightedCharacter("&", "AQUA"),
                                new SyntaxHighlightedCharacter("&", "AQUA")
                        )
                ),
                Arguments.of(
                        "(t)",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter("(", "YELLOW"),
                                new SyntaxHighlightedCharacter("t", "WHITE"),
                                new SyntaxHighlightedCharacter(")", "YELLOW")
                        )
                ),
                Arguments.of(
                        "(123)",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter("(", "YELLOW"),
                                new SyntaxHighlightedCharacter("1", "GREEN"),
                                new SyntaxHighlightedCharacter("2", "GREEN"),
                                new SyntaxHighlightedCharacter("3", "GREEN"),
                                new SyntaxHighlightedCharacter(")", "YELLOW")
                        )
                ),
                Arguments.of(
                        "(123.45)",
                        Arrays.asList(
                                new SyntaxHighlightedCharacter("(", "YELLOW"),
                                new SyntaxHighlightedCharacter("1", "GREEN"),
                                new SyntaxHighlightedCharacter("2", "GREEN"),
                                new SyntaxHighlightedCharacter("3", "GREEN"),
                                new SyntaxHighlightedCharacter(".", "GREEN"),
                                new SyntaxHighlightedCharacter("4", "GREEN"),
                                new SyntaxHighlightedCharacter("5", "GREEN"),
                                new SyntaxHighlightedCharacter(")", "YELLOW")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideInput")
    void Test_colors(String text, List<SyntaxHighlightedCharacter> expectedItems) {
        // Arrange
        SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter(SyntaxHighlighterColors.DEFAULT_COLORS);

        Lexer lexer = new Lexer(new Source("test", text), LexerTokenMappings.DEFAULT_MAPPINGS);
        lexer.scan();

        // Act
        List<SyntaxHighlightedCharacter> characters = syntaxHighlighter.highlight(text, lexer.getTokens());

        // Assert
        assertThat(characters).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedItems);
    }
}
