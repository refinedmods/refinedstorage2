package com.refinedmods.refinedstorage2.platform.fabric.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxModeDisplayProperties;
import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.api.grid.GridView;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiProxy;

public class ReiGridSearchBoxMode extends GridSearchBoxModeImpl {
    private final boolean twoWay;
    private final ReiProxy reiProxy;

    private ReiGridSearchBoxMode(GridQueryParser queryParser, boolean autoSelect, boolean twoWay, GridSearchBoxModeDisplayProperties displayProperties, ReiProxy reiProxy) {
        super(queryParser, autoSelect, displayProperties);
        this.twoWay = twoWay;
        this.reiProxy = reiProxy;
    }

    public static ReiGridSearchBoxMode create(GridQueryParser queryParser, boolean autoSelected, boolean twoWay, ReiProxy reiProxy) {
        return new ReiGridSearchBoxMode(queryParser, autoSelected, twoWay, new GridSearchBoxModeDisplayProperties(
                Rs2Mod.createIdentifier("textures/icons.png").toString(),
                autoSelected ? 16 : 0,
                96,
                createTranslationKey(autoSelected, twoWay)
        ), reiProxy);
    }

    private static String createTranslationKey(boolean autoSelected, boolean twoWay) {
        String twoWayText = twoWay ? "_two_way" : "";
        String autoSelectedText = autoSelected ? "_autoselected" : "";
        return Rs2Mod.createTranslationKey("gui", String.format("grid.search_box_mode.rei%s%s", twoWayText, autoSelectedText));
    }

    @Override
    public boolean onTextChanged(GridView<?> view, String text) {
        boolean success = super.onTextChanged(view, text);
        reiProxy.setSearchFieldText(text);
        return success;
    }

    @Override
    public String getSearchBoxValue() {
        if (twoWay) {
            return reiProxy.getSearchFieldText();
        }
        return null;
    }
}
