package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class NoOpGridSynchronizer extends AbstractGridSynchronizer {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronizer.off");
    private static final List<MutableComponent> HELP = List.of(createTranslation("gui", "grid.synchronizer.off.help"));

    @Override
    public MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    public List<MutableComponent> getHelp() {
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
