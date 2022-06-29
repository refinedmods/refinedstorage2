package com.refinedmods.refinedstorage2.platform.fabric.integration.jei;

import com.refinedmods.refinedstorage2.platform.apiimpl.grid.AbstractGridSynchronizer;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class JeiGridSynchronizer extends AbstractGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.jei");
    private static final MutableComponent TITLE_TWO_WAY = createTranslation("gui", "grid.synchronizer.jei.two_way");

    private final JeiProxy jeiProxy;
    private final boolean twoWay;

    public JeiGridSynchronizer(final JeiProxy jeiProxy, final boolean twoWay) {
        this.jeiProxy = jeiProxy;
        this.twoWay = twoWay;
    }

    @Override
    public MutableComponent getTitle() {
        return twoWay ? TITLE_TWO_WAY : TITLE;
    }

    @Override
    public void synchronizeFromGrid(final String text) {
        jeiProxy.setSearchFieldText(text);
    }

    @Override
    @Nullable
    public String getTextToSynchronizeToGrid() {
        return twoWay ? jeiProxy.getSearchFieldText() : null;
    }

    @Override
    public int getXTexture() {
        return twoWay ? 32 : 48;
    }
}
