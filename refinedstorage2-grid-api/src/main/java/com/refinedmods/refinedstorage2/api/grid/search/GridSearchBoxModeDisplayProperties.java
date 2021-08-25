package com.refinedmods.refinedstorage2.api.grid.search;

public record GridSearchBoxModeDisplayProperties(String textureIdentifier, int textureX, int textureY,
                                                 String nameTranslationKey) {
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
