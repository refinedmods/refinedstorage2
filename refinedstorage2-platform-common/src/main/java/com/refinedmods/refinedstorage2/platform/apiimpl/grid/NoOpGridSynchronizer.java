package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

public class NoOpGridSynchronizer extends DefaultGridSynchronizer {
    private static final MutableComponent TITLE = IdentifierUtil.createTranslation("gui", "grid.synchronizer.off");

    @Override
    public MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    public void synchronizeFromGrid(final String text) {
        // no op
    }

    @Override
    @Nullable
    public String getTextToSynchronizeToGrid() {
        return null;
    }

    @Override
    public int getXTexture() {
        return 64;
    }
}
