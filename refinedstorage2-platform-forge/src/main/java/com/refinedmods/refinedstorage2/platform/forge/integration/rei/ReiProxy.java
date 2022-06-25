package com.refinedmods.refinedstorage2.platform.forge.integration.rei;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;

public class ReiProxy {
    public String getSearchFieldText() {
        TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            return field.getText();
        }
        return null;
    }

    public void setSearchFieldText(String text) {
        TextField field = REIRuntime.getInstance().getSearchTextField();
        if (field != null) {
            field.setText(text);
        }
    }
}
