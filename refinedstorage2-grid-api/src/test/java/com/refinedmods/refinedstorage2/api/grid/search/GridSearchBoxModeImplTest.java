package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.FakeGridResource;
import com.refinedmods.refinedstorage2.api.grid.view.FakeGridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class GridSearchBoxModeImplTest {
    private final GridSearchBoxMode searchBoxMode = new GridSearchBoxModeImpl(new GridQueryParserImpl(
            LexerTokenMappings.DEFAULT_MAPPINGS,
            ParserOperatorMappings.DEFAULT_MAPPINGS,
            FakeGridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING
    ));

    @Test
    void Test_changing_text() {
        // Arrange
        GridView<String> view = new GridViewImpl<>(FakeGridResource::new, new ResourceListImpl<>());

        view.onChange("A", 64, null);
        view.onChange("B", 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "A");

        // Assert
        assertThat(view.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new FakeGridResource("A", 64)
        );
        assertThat(success).isTrue();
    }

    @Test
    void Test_changing_text_for_invalid_query() {
        // Arrange
        GridView<String> view = new GridViewImpl<>(FakeGridResource::new, new ResourceListImpl<>());

        view.onChange("A", 64, null);
        view.onChange("B", 64, null);

        // Act
        boolean success = searchBoxMode.onTextChanged(view, "|");

        // Assert
        assertThat(view.getAll()).isEmpty();
        assertThat(success).isFalse();
    }
}
