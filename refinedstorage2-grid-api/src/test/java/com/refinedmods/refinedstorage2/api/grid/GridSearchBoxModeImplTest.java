package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.test.ItemStubs;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage2.api.grid.GridStackAssertions.assertItemGridStackListContents;
import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridSearchBoxModeImplTest {
    private final GridSearchBoxMode searchBoxMode = new GridSearchBoxModeImpl(new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS), true, null);

    @Test
    void Test_changing_text() {
        // Arrange
        GridView<Rs2ItemStack> view = new GridViewImpl<>(new FakeGridStackFactory(), Rs2ItemStackIdentifier::new, StackListImpl.createItemStackList());

        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 64, null);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "dir");

        // Assert
        assertItemGridStackListContents(view.getStacks(), new Rs2ItemStack(ItemStubs.DIRT, 64));
        assertThat(success).isTrue();
    }

    @Test
    void Test_changing_text_for_invalid_query() {
        // Arrange
        GridView<Rs2ItemStack> view = new GridViewImpl<>(new FakeGridStackFactory(), Rs2ItemStackIdentifier::new, StackListImpl.createItemStackList());

        view.onChange(new Rs2ItemStack(ItemStubs.DIRT), 64, null);
        view.onChange(new Rs2ItemStack(ItemStubs.GLASS), 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "|");

        // Assert
        assertItemGridStackListContents(view.getStacks());
        assertThat(success).isFalse();
    }
}
