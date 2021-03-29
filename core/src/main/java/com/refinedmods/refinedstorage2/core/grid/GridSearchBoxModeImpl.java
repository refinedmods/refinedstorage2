package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserException;

public class GridSearchBoxModeImpl implements GridSearchBoxMode {
    // TODO - Supply as parameter
    private static final GridQueryParser QUERY_PARSER = new GridQueryParser();

    private final boolean autoSelect;
    private final GridSearchBoxModeDisplayProperties displayProperties;

    public GridSearchBoxModeImpl(boolean autoSelect, GridSearchBoxModeDisplayProperties displayProperties) {
        this.autoSelect = autoSelect;
        this.displayProperties = displayProperties;
    }

    @Override
    public void onTextChanged(GridView<?> view, String text) {
        try {
            view.setFilter(QUERY_PARSER.parse(text));
        } catch (GridQueryParserException e) {
            view.setFilter(stack -> false);
        }
        view.sort();
    }

    @Override
    public String getSearchBoxValue() {
        return null;
    }

    @Override
    public GridSearchBoxModeDisplayProperties getDisplayProperties() {
        return displayProperties;
    }

    @Override
    public boolean shouldAutoSelect() {
        return autoSelect;
    }
}
