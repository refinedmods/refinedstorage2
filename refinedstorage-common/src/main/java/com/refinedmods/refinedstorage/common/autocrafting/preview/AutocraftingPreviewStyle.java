package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import net.minecraft.network.chat.Component;

public enum AutocraftingPreviewStyle {
    LIST("list"),
    TREE("tree");

    private final Component name;

    AutocraftingPreviewStyle(final String name) {
        this.name = IdentifierUtil.createTranslation("gui", "autocrafting_preview.style." + name);
    }

    public Component getName() {
        return name;
    }

    public AutocraftingPreviewStyle next() {
        return this == LIST ? TREE : LIST;
    }
}
