package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;

import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridSearchBoxModeImpl implements GridSearchBoxMode {
    private final GridQueryParser queryParser;

    public GridSearchBoxModeImpl(GridQueryParser queryParser) {
        this.queryParser = queryParser;
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
}
