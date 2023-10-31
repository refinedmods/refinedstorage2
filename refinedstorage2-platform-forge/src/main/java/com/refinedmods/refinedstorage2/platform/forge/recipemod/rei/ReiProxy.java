package com.refinedmods.refinedstorage2.platform.forge.recipemod.rei;

import javax.annotation.Nullable;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;

public class ReiProxy {
    @Nullable
    public String getSearchFieldText() {
        final TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            return field.getText();
        }
        return null;
    }

    public void setSearchFieldText(final String text) {
        final TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            field.setText(text);
        }
    }
}
