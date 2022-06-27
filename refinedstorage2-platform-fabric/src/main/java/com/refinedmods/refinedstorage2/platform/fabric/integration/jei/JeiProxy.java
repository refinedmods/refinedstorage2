package com.refinedmods.refinedstorage2.platform.fabric.integration.jei;

import mezz.jei.api.runtime.IJeiRuntime;

public class JeiProxy {
    public String getSearchFieldText() {
        IJeiRuntime runtime = JeiPlugin.getRuntime();
        if (runtime == null) {
            return "";
        }
        return runtime.getIngredientFilter().getFilterText();
    }

    public void setSearchFieldText(final String text) {
        IJeiRuntime runtime = JeiPlugin.getRuntime();
        if (runtime != null) {
            runtime.getIngredientFilter().setFilterText(text);
        }
    }
}
