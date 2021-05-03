package com.refinedmods.refinedstorage2.fabric.integration.rei;

import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.widget.TextFieldWidget;

public class ReiProxy {
    public void setSearchFieldText(String text) {
        TextFieldWidget field = REIHelper.getInstance().getSearchTextField();
        if (field != null) {
            field.setText(text);
        }
    }

    public String getSearchFieldText() {
        TextFieldWidget field = REIHelper.getInstance().getSearchTextField();
        if (field != null) {
            return field.getText();
        }
        return null;
    }
}
