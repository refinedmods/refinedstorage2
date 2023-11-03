package com.refinedmods.refinedstorage2.platform.common.recipemod.jei;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridSynchronizer;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class JeiGridSynchronizer extends AbstractGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.jei");
    private static final MutableComponent TITLE_TWO_WAY = createTranslation("gui", "grid.synchronizer.jei.two_way");
    private static final Component HELP = createTranslation("gui", "grid.synchronizer.jei.help");
    private static final Component HELP_TWO_WAY = createTranslation("gui", "grid.synchronizer.jei.two_way.help");

    private final JeiProxy jeiProxy;
    private final boolean twoWay;

    JeiGridSynchronizer(final JeiProxy jeiProxy, final boolean twoWay) {
        this.jeiProxy = jeiProxy;
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
