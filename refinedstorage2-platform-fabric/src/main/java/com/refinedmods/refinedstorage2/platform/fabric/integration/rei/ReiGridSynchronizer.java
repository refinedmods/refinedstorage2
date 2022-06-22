package com.refinedmods.refinedstorage2.platform.fabric.integration.rei;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ReiGridSynchronizer implements GridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronization.rei");

    private final ReiProxy reiProxy;

    public ReiGridSynchronizer(ReiProxy reiProxy) {
        this.reiProxy = reiProxy;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public void synchronizeFromGrid(String text) {
        reiProxy.setSearchFieldText(text);
    }

    @Override
    public String getTextToSynchronizeToGrid() {
        return reiProxy.getSearchFieldText();
    }
}
