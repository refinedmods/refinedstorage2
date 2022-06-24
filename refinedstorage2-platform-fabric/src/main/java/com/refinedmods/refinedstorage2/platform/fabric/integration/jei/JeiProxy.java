package com.refinedmods.refinedstorage2.platform.fabric.integration.jei;

public class JeiProxy {
    public String getSearchFieldText() {
        return JeiPlugin.getRuntime().getIngredientFilter().getFilterText();
    }

    public void setSearchFieldText(String text) {
        JeiPlugin.getRuntime().getIngredientFilter().setFilterText(text);
    }
}
