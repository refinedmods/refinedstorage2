package com.refinedmods.refinedstorage2.platform.fabric.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridSynchronizer;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ReiGridSynchronizer extends AbstractGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.rei");
    private static final MutableComponent TITLE_TWO_WAY = createTranslation("gui", "grid.synchronizer.rei.two_way");
    private static final Component HELP = createTranslation("gui", "grid.synchronizer.rei.help");
    private static final Component HELP_TWO_WAY = createTranslation("gui", "grid.synchronizer.rei.two_way.help");

    private final ReiProxy reiProxy;
    private final boolean twoWay;

    public ReiGridSynchronizer(final ReiProxy reiProxy, final boolean twoWay) {
        this.reiProxy = reiProxy;
        this.twoWay = twoWay;
    }

    @Override
    public MutableComponent getTitle() {
        return twoWay ? TITLE_TWO_WAY : TITLE;
    }

    @Override
    public Component getHelpText() {
        return twoWay ? HELP_TWO_WAY : HELP;
    }

    @Override
    public void synchronizeFromGrid(final String text) {
        reiProxy.setSearchFieldText(text);
    }

    @Override
    @Nullable
    public String getTextToSynchronizeToGrid() {
        return twoWay ? reiProxy.getSearchFieldText() : null;
    }

    @Override
    public int getXTexture() {
        return twoWay ? 32 : 48;
    }
}
