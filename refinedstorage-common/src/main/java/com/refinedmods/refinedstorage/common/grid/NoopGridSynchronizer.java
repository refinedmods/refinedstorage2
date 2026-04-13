package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.GridSynchronizer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NoopGridSynchronizer implements GridSynchronizer {
    public static final NoopGridSynchronizer INSTANCE = new NoopGridSynchronizer();
    @API(status = API.Status.INTERNAL)
    public static final Identifier ON = createIdentifier("widget/side_button/grid/synchronization_mode/on");
    @API(status = API.Status.INTERNAL)
    public static final Identifier ON_TWO_WAY =
        createIdentifier("widget/side_button/grid/synchronization_mode/on_two_way");

    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronization_mode.off");
    private static final Component HELP = createTranslation("gui", "grid.synchronization_mode.off.help");
    private static final Identifier OFF = createIdentifier("widget/side_button/grid/synchronization_mode/off");

    private NoopGridSynchronizer() {
    }

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
    public Identifier getSprite() {
        return OFF;
    }
}
