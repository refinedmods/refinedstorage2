package com.refinedmods.refinedstorage2.core.grid;

public class GridSearchBoxModeDisplayProperties {
    private final String textureIdentifier;
    private final int textureX;
    private final int textureY;
    private final String nameTranslationKey;

    public GridSearchBoxModeDisplayProperties(String textureIdentifier, int textureX, int textureY, String nameTranslationKey) {
        this.textureIdentifier = textureIdentifier;
        this.textureX = textureX;
        this.textureY = textureY;
        this.nameTranslationKey = nameTranslationKey;
    }

    public String getTextureIdentifier() {
        return textureIdentifier;
    }

    public int getTextureX() {
        return textureX;
    }

    public int getTextureY() {
        return textureY;
    }

    public String getNameTranslationKey() {
        return nameTranslationKey;
    }
}
