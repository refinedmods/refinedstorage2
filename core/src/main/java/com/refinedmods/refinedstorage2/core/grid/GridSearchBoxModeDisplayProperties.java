package com.refinedmods.refinedstorage2.core.grid;

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
