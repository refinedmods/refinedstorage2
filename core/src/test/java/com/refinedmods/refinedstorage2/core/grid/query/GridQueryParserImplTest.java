package com.refinedmods.refinedstorage2.core.grid.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.core.grid.ItemGridStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class GridQueryParserImplTest {
    private final GridQueryParser queryParser = new GridQueryParserImpl();

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void Test_empty_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dirt", "Dirt", "DiRt", "Di", "irt"})
    void Test_name_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@refined", "@\"Refined Storage\"", "@ReFiNe", "@Storage", "@rs", "@RS"})
    void Test_mod_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.SPONGE), "rs", "Refined Storage"))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"$underwater", "$UnDerWate", "$water", "$unrelated", "$UNREL", "$laTed"})
    void Test_tag_query(String query) throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.SPONGE), "mc", "Minecraft", "underwater", "unrelated"))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS), "mc", "Minecraft", "transparent"))).isFalse();
    }

    @Test
    void Test_mod_query_with_invalid_node() {
        // Act
        Executable action = () -> queryParser.parse("@!true");

        // Assert
        GridQueryParserException e = assertThrows(GridQueryParserException.class, action);
        assertThat(e.getMessage()).isEqualTo("Mod filtering expects a literal");
    }

    @Test
    void Test_implicit_and_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("DirT di RT");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isFalse();
    }

    @Test
    void Test_and_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("DirT && di && RT");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isFalse();
    }

    @Test
    void Test_or_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("dir || glass || StoNe");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.STONE)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.COBBLESTONE)))).isTrue();

        assertThat(predicate.test(stack(new ItemStack(Items.SPONGE)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.FURNACE)))).isFalse();
    }

    @Test
    void Test_simple_not_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("!stone");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isTrue();

        assertThat(predicate.test(stack(new ItemStack(Items.STONE)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.COBBLESTONE)))).isFalse();
    }

    @Test
    void Test_not_query_with_multiple_and_parts() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("!(stone || dirt)");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.SPONGE)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS)))).isTrue();

        assertThat(predicate.test(stack(new ItemStack(Items.STONE)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.DIRT)))).isFalse();
    }

    @Test
    void Test_complex_mod_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("((spo || buck) && @refined) || (glass && @mine)");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.SPONGE), "rs", "Refined Storage"))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.BUCKET), "rs", "Refined Storage"))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.SADDLE), "rs", "Refined Storage"))).isFalse();

        assertThat(predicate.test(stack(new ItemStack(Items.GLASS), "mc", "Minecraft"))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.FURNACE), "mc", "Minecraft"))).isFalse();
    }

    @Test
    void Test_less_than_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("<5");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 5)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 4)))).isTrue();
    }

    @Test
    void Test_less_than_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("<=5");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 6)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 5)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 4)))).isTrue();
    }

    @Test
    void Test_greater_than_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(">5");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 5)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 6)))).isTrue();
    }

    @Test
    void Test_greater_than_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse(">=5");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 4)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 5)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 6)))).isTrue();
    }

    @Test
    void Test_equals_count_query() throws GridQueryParserException {
        // Act
        Predicate<GridStack<?>> predicate = queryParser.parse("=5");

        // Assert
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 4)))).isFalse();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 5)))).isTrue();
        assertThat(predicate.test(stack(new ItemStack(Items.GLASS, 6)))).isFalse();
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

    private GridStack<ItemStack> stack(ItemStack stack) {
        return stack(stack, "mc", "Minecraft");
    }

    private GridStack<ItemStack> stack(ItemStack stack, String modId, String modName, String... tags) {
        return new ItemGridStack(
            stack,
            stack.getName().getString(),
            modId,
            modName,
            new HashSet<>(Arrays.asList(tags))
        );
    }
}
