package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;

import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class PlatformSearchBoxModeImpl extends GridSearchBoxModeImpl {
    private final Identifier textureIdentifier;
    private final int textureX;
    private final int textureY;
    private final TranslatableText name;
    private final boolean autoSelected;

    public PlatformSearchBoxModeImpl(GridQueryParser queryParser, Identifier textureIdentifier, int textureX, int textureY, TranslatableText name, boolean autoSelected) {
        super(queryParser);
        this.textureIdentifier = textureIdentifier;
        this.textureX = textureX;
        this.textureY = textureY;
        this.name = name;
        this.autoSelected = autoSelected;
    }

    public Identifier getTextureIdentifier() {
        return textureIdentifier;
    }

    public int getTextureX() {
        return textureX;
    }

    public int getTextureY() {
        return textureY;
    }

    public TranslatableText getName() {
        return name;
    }

    public boolean isAutoSelected() {
        return autoSelected;
    }

    public String getOverrideSearchBoxValue() {
        return null;
    }
}
