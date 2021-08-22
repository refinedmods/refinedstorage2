package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserException;

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
    public boolean onTextChanged(GridView<?> view, String text) {
        boolean success = true;
        try {
            view.setFilter(queryParser.parse(text));
        } catch (GridQueryParserException e) {
            view.setFilter(stack -> false);
            success = false;
        }
        view.sort();
        return success;
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
