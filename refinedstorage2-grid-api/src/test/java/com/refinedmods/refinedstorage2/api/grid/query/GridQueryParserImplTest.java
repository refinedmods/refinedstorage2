package com.refinedmods.refinedstorage2.api.grid.query;

import com.refinedmods.refinedstorage2.api.grid.view.FakeGridResource;
import com.refinedmods.refinedstorage2.api.grid.view.FakeGridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rs2Test
class GridQueryParserImplTest {
    private final GridQueryParser<String> queryParser = new GridQueryParserImpl<>(
            LexerTokenMappings.DEFAULT_MAPPINGS,
            ParserOperatorMappings.DEFAULT_MAPPINGS,
            FakeGridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING
    );

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void Test_empty_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dirt", "Dirt", "DiRt", "Di", "irt"})
    void Test_name_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@refined", "@\"Refined Storage\"", "@ReFiNe", "@Storage", "@rs", "@RS"})
    void Test_mod_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new FakeGridResource("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"$underwater", "$UnDerWate", "$water", "$unrelated", "$UNREL", "$laTed"})
    void Test_tag_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new FakeGridResource("Sponge", 1, "mc", "Minecraft", Set.of("underwater", "unrelated")))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Dirt", 1, "mc", "Minecraft", Set.of("transparent")))).isFalse();
    }

    @Test
    void Test_attribute_query_with_invalid_node() {
        // Act
        Executable action = () -> queryParser.parse("@!true");

        // Assert
        GridQueryParserException e = assertThrows(GridQueryParserException.class, action);
        assertThat(e.getMessage()).isEqualTo("Expected a literal");
    }

    @Test
    void Test_implicit_and_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("DirT di RT");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isFalse();
    }

    @Test
    void Test_implicit_and_query_in_parenthesis() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("(DirT di RT) || (sto stone)");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Stone"))).isTrue();
    }

    @Test
    void Test_implicit_and_query_with_unary_operator() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("@minecraft >5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt", 6, "minecraft", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass", 5, "minecraft", "Minecraft", Set.of()))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Sponge", 5, "rs", "Refined Storage", Set.of()))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Cobblestone", 6, "rs", "Refined Storage", Set.of()))).isFalse();
    }

    @Test
    void Test_and_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("DirT && di && RT");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isFalse();
    }

    @Test
    void Test_or_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("dir || glass || StoNe");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Stone"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Cobblestone"))).isTrue();

        assertThat(predicate.test(new FakeGridResource("Sponge"))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Furnace"))).isFalse();
    }

    @Test
    void Test_simple_not_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("!stone");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isTrue();

        assertThat(predicate.test(new FakeGridResource("Stone"))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Cobblestone"))).isFalse();
    }

    @Test
    void Test_not_query_with_multiple_and_parts() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("!(stone || dirt)");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Sponge"))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass"))).isTrue();

        assertThat(predicate.test(new FakeGridResource("Stone"))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Dirt"))).isFalse();
    }

    @Test
    void Test_complex_mod_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("((spo || buck) && @refined) || (glass && @mine)");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Bucket", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Saddle", 1, "rs", "Refined Storage", Set.of()))).isFalse();

        assertThat(predicate.test(new FakeGridResource("Glass", 1, "mc", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Furnace", 1, "mc", "Minecraft", Set.of()))).isFalse();
    }

    @Test
    void Test_less_than_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("<5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Glass", 5))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Glass", 4))).isTrue();
    }

    @Test
    void Test_less_than_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("<=5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Glass", 6))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Glass", 5))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass", 4))).isTrue();
    }

    @Test
    void Test_greater_than_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(">5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Glass", 5))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Glass", 6))).isTrue();
    }

    @Test
    void Test_greater_than_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse(">=5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Glass", 4))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Glass", 5))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass", 6))).isTrue();
    }

    @Test
    void Test_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridResource<String>> predicate = queryParser.parse("=5");

        // Assert
        assertThat(predicate.test(new FakeGridResource("Glass", 4))).isFalse();
        assertThat(predicate.test(new FakeGridResource("Glass", 5))).isTrue();
        assertThat(predicate.test(new FakeGridResource("Glass", 6))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void Test_invalid_node_in_unary_count_operator(String operator) {
        // Act
        GridQueryParserException e = assertThrows(GridQueryParserException.class, () -> queryParser.parse(operator + "(1 && 1)"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects a literal");
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void Test_invalid_token_in_unary_count_operator_literal(String operator) {
        // Act
        GridQueryParserException e = assertThrows(GridQueryParserException.class, () -> queryParser.parse(operator + "hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects an integer number");
    }
}
