package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlatformSearchBoxModeImpl extends GridSearchBoxModeImpl {
    private final ResourceLocation textureIdentifier;
    private final int textureX;
    private final int textureY;
    private final TranslatableComponent name;
    private final boolean autoSelected;

    public PlatformSearchBoxModeImpl(GridQueryParser queryParser, ResourceLocation textureIdentifier, int textureX, int textureY, TranslatableComponent name, boolean autoSelected) {
        super(queryParser);
        this.textureIdentifier = textureIdentifier;
        this.textureX = textureX;
        this.textureY = textureY;
        this.name = name;
        this.autoSelected = autoSelected;
    }

    public ResourceLocation getTextureIdentifier() {
        return textureIdentifier;
    }

    public int getTextureX() {
        return textureX;
    }

    public int getTextureY() {
        return textureY;
    }

    public TranslatableComponent getName() {
        return name;
    }

    public boolean isAutoSelected() {
        return autoSelected;
    }

    public String getOverrideSearchBoxValue() {
        return null;
    }
}
