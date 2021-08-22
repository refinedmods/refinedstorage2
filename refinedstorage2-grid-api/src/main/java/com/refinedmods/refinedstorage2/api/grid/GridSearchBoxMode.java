package com.refinedmods.refinedstorage2.api.grid;

public interface GridSearchBoxMode {
    boolean onTextChanged(GridView<?> view, String text);

    String getSearchBoxValue();

    GridSearchBoxModeDisplayProperties getDisplayProperties();

    boolean shouldAutoSelect();
}
