package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;

import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
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
            view.setFilter((Predicate) queryParser.parse(text));
        } catch (GridQueryParserException e) {
            view.setFilter(resource -> false);
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
