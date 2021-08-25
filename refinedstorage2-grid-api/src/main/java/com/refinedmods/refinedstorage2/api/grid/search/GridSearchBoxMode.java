package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.view.GridView;

public interface GridSearchBoxMode {
    boolean onTextChanged(GridView<?> view, String text);

    String getSearchBoxValue();

    GridSearchBoxModeDisplayProperties getDisplayProperties();

    boolean shouldAutoSelect();
}
