package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiProxy;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ReiGridSearchBoxMode extends PlatformSearchBoxModeImpl {
    private final boolean twoWay;
    private final ReiProxy reiProxy;

    public ReiGridSearchBoxMode(GridQueryParser queryParser, ResourceLocation textureIdentifier, int textureX, int textureY, TranslatableComponent name, boolean autoSelect, boolean twoWay, ReiProxy reiProxy) {
        super(queryParser, textureIdentifier, textureX, textureY, name, autoSelect);
        this.twoWay = twoWay;
        this.reiProxy = reiProxy;
    }

    public static ReiGridSearchBoxMode create(GridQueryParser queryParser, boolean autoSelected, boolean twoWay, ReiProxy reiProxy) {
        return new ReiGridSearchBoxMode(
                queryParser,
                Rs2Mod.createIdentifier("textures/icons.png"),
                autoSelected ? 16 : 0,
                96,
                createTranslation(autoSelected, twoWay),
                autoSelected,
                twoWay,
                reiProxy
        );
    }

    private static TranslatableComponent createTranslation(boolean autoSelected, boolean twoWay) {
        String twoWayText = twoWay ? "_two_way" : "";
        String autoSelectedText = autoSelected ? "_autoselected" : "";
        return Rs2Mod.createTranslation("gui", String.format("grid.search_box_mode.rei%s%s", twoWayText, autoSelectedText));
    }

    @Override
    public boolean onTextChanged(GridView<?> view, String text) {
        boolean success = super.onTextChanged(view, text);
        reiProxy.setSearchFieldText(text);
        return success;
    }

    @Override
    public String getOverrideSearchBoxValue() {
        if (twoWay) {
            return reiProxy.getSearchFieldText();
        }
        return null;
    }
}
