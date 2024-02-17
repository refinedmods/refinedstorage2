package com.refinedmods.refinedstorage2.platform.common.grid;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class NoopGridSynchronizer extends AbstractGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.off");
    private static final Component HELP = createTranslation("gui", "grid.synchronizer.off.help");

    @Override
    public MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    public Component getHelpText() {
        return HELP;
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
