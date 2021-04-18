package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.core.util.ItemStackIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.core.util.ItemStackAssertions.assertItemGridStackListContents;

@RefinedStorage2Test
class GridSearchBoxModeImplTest {
    private final GridSearchBoxMode searchBoxMode = new GridSearchBoxModeImpl(new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS), true, null);

    @Test
    void Test_changing_text() {
        // Arrange
        GridView<ItemStack> view = new GridViewImpl<>(new FakeGridStackFactory(), ItemStackIdentifier::new, ItemStackList.create());

        view.onChange(new ItemStack(Items.DIRT), 64, null);
        view.onChange(new ItemStack(Items.GLASS), 64, null);

        // Act
        searchBoxMode.onTextChanged(view, "dir");

        // Assert
        assertItemGridStackListContents(view.getStacks(), new ItemStack(Items.DIRT, 64));
    }

    @Test
    void Test_changing_text_for_invalid_query() {
        // Arrange
        GridView<ItemStack> view = new GridViewImpl<>(new FakeGridStackFactory(), ItemStackIdentifier::new, ItemStackList.create());

        view.onChange(new ItemStack(Items.DIRT), 64, null);
        view.onChange(new ItemStack(Items.GLASS), 64, null);

        // Act
        searchBoxMode.onTextChanged(view, "|");

        // Assert
        assertItemGridStackListContents(view.getStacks());
    }
}
