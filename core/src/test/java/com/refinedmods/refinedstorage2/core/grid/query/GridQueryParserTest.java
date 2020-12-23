package com.refinedmods.refinedstorage2.core.grid.query;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RefinedStorage2Test
class GridQueryParserTest {
    @ParameterizedTest
    @ValueSource(strings = {"dirt", "Dirt", "DiRt", "Di", "irt"})
    void Test_name_query(String query) throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@refined", "@\"Refined Storage\"", "@ReFiNe", "@Storage", "@rs", "@RS"})
    void Test_mod_query(String query) throws GridQueryParserException {
        // Arrange
        FakeGridStackDetailsProvider detailsProvider = new FakeGridStackDetailsProvider();

        detailsProvider.setModName(Items.SPONGE, "Refined Storage");
        detailsProvider.setModId(Items.SPONGE, "rs");

        detailsProvider.setModName(Items.GLASS, "Minecraft");

        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(detailsProvider);

        // Act
        Predicate<ItemStack> predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(new ItemStack(Items.SPONGE))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isFalse();
    }

    @Test
    void Test_mod_query_with_invalid_node() {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Executable action = () -> queryParser.parse("@!true");

        // Assert
        GridQueryParserException e = assertThrows(GridQueryParserException.class, action);
        assertThat(e.getMessage()).isEqualTo("Mod filtering expects a literal");
    }

    @Test
    void Test_implicit_and_query() throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("DirT di RT");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isFalse();
    }

    @Test
    void Test_and_query() throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("DirT && di && RT");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isFalse();
    }

    @Test
    void Test_or_query() throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("dir || glass || StoNe");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.STONE))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.COBBLESTONE))).isTrue();

        assertThat(predicate.test(new ItemStack(Items.SPONGE))).isFalse();
        assertThat(predicate.test(new ItemStack(Items.FURNACE))).isFalse();
    }

    @Test
    void Test_simple_not_query() throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("!stone");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isTrue();

        assertThat(predicate.test(new ItemStack(Items.STONE))).isFalse();
        assertThat(predicate.test(new ItemStack(Items.COBBLESTONE))).isFalse();
    }

    @Test
    void Test_not_query_with_multiple_and_parts() throws GridQueryParserException {
        // Arrange
        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(new FakeGridStackDetailsProvider());

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("!(stone || dirt)");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.SPONGE))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.GLASS))).isTrue();

        assertThat(predicate.test(new ItemStack(Items.STONE))).isFalse();
        assertThat(predicate.test(new ItemStack(Items.DIRT))).isFalse();
    }

    @Test
    void Test_complex_mod_query() throws GridQueryParserException {
        // Arrange
        FakeGridStackDetailsProvider detailsProvider = new FakeGridStackDetailsProvider();

        detailsProvider.setModName(Items.SPONGE, "Refined Storage");
        detailsProvider.setModName(Items.BUCKET, "Refined Storage");
        detailsProvider.setModName(Items.SADDLE, "Refined Storage");

        GridQueryParser<ItemStack> queryParser = new GridQueryParser<>(detailsProvider);

        // Act
        Predicate<ItemStack> predicate = queryParser.parse("((spo || buck) && @refined) || (glass && @mine)");

        // Assert
        assertThat(predicate.test(new ItemStack(Items.SPONGE))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.BUCKET))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.SADDLE))).isFalse();

        assertThat(predicate.test(new ItemStack(Items.GLASS))).isTrue();
        assertThat(predicate.test(new ItemStack(Items.FURNACE))).isFalse();
    }
}
