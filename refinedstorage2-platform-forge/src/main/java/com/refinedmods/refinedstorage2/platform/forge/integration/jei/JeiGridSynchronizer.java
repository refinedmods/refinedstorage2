package com.refinedmods.refinedstorage2.platform.forge.integration.jei;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class JeiGridSynchronizer implements GridSynchronizer {
    private static final TranslatableComponent TITLE = createTranslation("gui", "grid.synchronization.jei");

    private final JeiProxy jeiProxy;

    public JeiGridSynchronizer(JeiProxy jeiProxy) {
        this.jeiProxy = jeiProxy;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public void synchronizeFromGrid(String text) {
        jeiProxy.setSearchFieldText(text);
    }

    @Override
    public String getTextToSynchronizeToGrid() {
        return jeiProxy.getSearchFieldText();
    }
}
