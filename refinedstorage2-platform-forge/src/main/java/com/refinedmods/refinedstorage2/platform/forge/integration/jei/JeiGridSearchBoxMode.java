package com.refinedmods.refinedstorage2.platform.forge.integration.jei;

import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class JeiGridSearchBoxMode extends PlatformSearchBoxModeImpl {
    private final boolean twoWay;
    private final JeiProxy jeiProxy;

    public JeiGridSearchBoxMode(GridQueryParser queryParser, ResourceLocation textureIdentifier, int textureX, int textureY, TranslatableComponent name, boolean autoSelect, boolean twoWay, JeiProxy jeiProxy) {
        super(queryParser, textureIdentifier, textureX, textureY, name, autoSelect);
        this.twoWay = twoWay;
        this.jeiProxy = jeiProxy;
    }

    public static JeiGridSearchBoxMode create(GridQueryParser queryParser, boolean autoSelected, boolean twoWay, JeiProxy jeiProxy) {
        return new JeiGridSearchBoxMode(
                queryParser,
                createIdentifier("textures/icons.png"),
                autoSelected ? 16 : 0,
                96,
                createTranslation(autoSelected, twoWay),
                autoSelected,
                twoWay,
                jeiProxy
        );
    }

    private static TranslatableComponent createTranslation(boolean autoSelected, boolean twoWay) {
        String twoWayText = twoWay ? "_two_way" : "";
        String autoSelectedText = autoSelected ? "_autoselected" : "";
        return IdentifierUtil.createTranslation("gui", String.format("grid.search_box_mode.jei%s%s", twoWayText, autoSelectedText));
    }

    @Override
    public boolean onTextChanged(GridView<?> view, String text) {
        boolean success = super.onTextChanged(view, text);
        jeiProxy.setSearchFieldText(text);
        return success;
    }

    @Override
    public String getOverrideSearchBoxValue() {
        if (twoWay) {
            return jeiProxy.getSearchFieldText();
        }
        return null;
    }
}
