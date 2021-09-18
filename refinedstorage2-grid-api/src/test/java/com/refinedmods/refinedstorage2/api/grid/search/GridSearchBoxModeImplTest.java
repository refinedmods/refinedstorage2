package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.FakeGridStack;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridSearchBoxModeImplTest {
    private final GridSearchBoxMode searchBoxMode = new GridSearchBoxModeImpl(new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS), true, null);

    @Test
    void Test_changing_text() {
        // Arrange
        GridView<String> view = new GridViewImpl<>(FakeGridStack::new, new StackListImpl<>());

        view.onChange("A", 64, null);
        view.onChange("B", 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "A");

        // Assert
        assertThat(view.getStacks()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridStack("A", 64)
        );
        assertThat(success).isTrue();
    }

    @Test
    void Test_changing_text_for_invalid_query() {
        // Arrange
        GridView<String> view = new GridViewImpl<>(FakeGridStack::new, new StackListImpl<>());

        view.onChange("A", 64, null);
        view.onChange("B", 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "|");

        // Assert
        assertThat(view.getStacks()).isEmpty();
        assertThat(success).isFalse();
    }
}
