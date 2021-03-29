package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserException;

public class GridSearchBoxModeImpl implements GridSearchBoxMode {
    private final GridQueryParser queryParser;
    private final boolean autoSelect;
    private final GridSearchBoxModeDisplayProperties displayProperties;

    public GridSearchBoxModeImpl(GridQueryParser queryParser, boolean autoSelect, GridSearchBoxModeDisplayProperties displayProperties) {
        this.queryParser = queryParser;
        this.autoSelect = autoSelect;
        this.displayProperties = displayProperties;
    }

    @Override
    public void onTextChanged(GridView<?> view, String text) {
        try {
            view.setFilter(queryParser.parse(text));
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
