package com.refinedmods.refinedstorage2.api.grid.search;

import com.refinedmods.refinedstorage2.api.grid.view.GridView;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridSearchBoxMode {
    /**
     * Called when the search text has changed. Use this to modify the given grid view.
     *
     * @param view the view
     * @param text the search query
     * @return whether the search operation was successful
     */
    boolean onTextChanged(GridView<?> view, String text);

    /**
     * @return the search box value if it has to be modified, otherwise null
     */
    // TODO: mc specific
    String getSearchBoxValue();

    /**
     * @return the display properties
     */
    // TODO: mc specific
    GridSearchBoxModeDisplayProperties getDisplayProperties();

    /**
     * @return whether this search box mode autoselects
     */
    // TODO: mc specific
    boolean shouldAutoSelect();
}
