package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;

public final class KeyMappings {
    public static final KeyMappings INSTANCE = new KeyMappings();

    @Nullable
    private KeyMapping focusSearchBar;

    private KeyMappings() {
    }

    public KeyMapping getFocusSearchBar() {
        return Objects.requireNonNull(focusSearchBar);
    }

    public void setFocusSearchBar(final KeyMapping focusSearchBar) {
        this.focusSearchBar = focusSearchBar;
    }
}
