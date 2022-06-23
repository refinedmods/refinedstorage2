package com.refinedmods.refinedstorage2.platform.fabric.integration.rei;

import com.refinedmods.refinedstorage2.platform.apiimpl.grid.DefaultGridSynchronizer;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ReiGridSynchronizer extends DefaultGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.rei");
    private static final MutableComponent TITLE_TWO_WAY = createTranslation("gui", "grid.synchronizer.rei.two_way");

    private final ReiProxy reiProxy;
    private final boolean twoWay;

    public ReiGridSynchronizer(ReiProxy reiProxy, boolean twoWay) {
        this.reiProxy = reiProxy;
        this.twoWay = twoWay;
    }

    @Override
    public MutableComponent getTitle() {
        return twoWay ? TITLE_TWO_WAY : TITLE;
    }

    @Override
    public void synchronizeFromGrid(String text) {
        reiProxy.setSearchFieldText(text);
    }

    @Override
    public String getTextToSynchronizeToGrid() {
        return twoWay ? reiProxy.getSearchFieldText() : null;
    }

    @Override
    public int getXTexture() {
        return twoWay ? 32 : 48;
    }
}
