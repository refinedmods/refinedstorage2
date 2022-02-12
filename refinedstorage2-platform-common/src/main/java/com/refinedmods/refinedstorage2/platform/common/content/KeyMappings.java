package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.client.KeyMapping;

public final class KeyMappings {
    public static final KeyMappings INSTANCE = new KeyMappings();

    private KeyMapping focusSearchBar;

    private KeyMappings() {
    }

    public KeyMapping getFocusSearchBar() {
        return focusSearchBar;
    }

    public void setFocusSearchBar(KeyMapping focusSearchBar) {
        this.focusSearchBar = focusSearchBar;
    }
}
