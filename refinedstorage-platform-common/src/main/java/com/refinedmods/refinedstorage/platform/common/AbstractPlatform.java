package com.refinedmods.refinedstorage.platform.common;

import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.MenuOpener;
import com.refinedmods.refinedstorage.platform.common.support.render.FluidRenderer;

public abstract class AbstractPlatform implements Platform {
    private final MenuOpener menuOpener;
    private final FluidRenderer fluidRenderer;
    private final GridInsertionStrategyFactory defaultGridInsertionStrategyFactory;

    protected AbstractPlatform(final MenuOpener menuOpener,
                               final FluidRenderer fluidRenderer,
                               final GridInsertionStrategyFactory defaultGridInsertionStrategyFactory) {
        this.menuOpener = menuOpener;
        this.fluidRenderer = fluidRenderer;
        this.defaultGridInsertionStrategyFactory = defaultGridInsertionStrategyFactory;
    }

    @Override
    public MenuOpener getMenuOpener() {
        return menuOpener;
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return fluidRenderer;
    }

    @Override
    public GridInsertionStrategyFactory getDefaultGridInsertionStrategyFactory() {
        return defaultGridInsertionStrategyFactory;
    }
}
