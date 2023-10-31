package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.support.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.support.render.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.common.util.BucketAmountFormatting;

public abstract class AbstractPlatform implements Platform {
    private final ServerToClientCommunications serverToClientCommunications;
    private final ClientToServerCommunications clientToServerCommunications;
    private final MenuOpener menuOpener;
    private final BucketAmountFormatting bucketAmountFormatting;
    private final FluidRenderer fluidRenderer;
    private final GridInsertionStrategyFactory defaultGridInsertionStrategyFactory;

    protected AbstractPlatform(final ServerToClientCommunications serverToClientCommunications,
                               final ClientToServerCommunications clientToServerCommunications,
                               final MenuOpener menuOpener,
                               final BucketAmountFormatting bucketAmountFormatting,
                               final FluidRenderer fluidRenderer,
                               final GridInsertionStrategyFactory defaultGridInsertionStrategyFactory) {
        this.serverToClientCommunications = serverToClientCommunications;
        this.clientToServerCommunications = clientToServerCommunications;
        this.menuOpener = menuOpener;
        this.bucketAmountFormatting = bucketAmountFormatting;
        this.fluidRenderer = fluidRenderer;
        this.defaultGridInsertionStrategyFactory = defaultGridInsertionStrategyFactory;
    }

    @Override
    public ServerToClientCommunications getServerToClientCommunications() {
        return serverToClientCommunications;
    }

    @Override
    public ClientToServerCommunications getClientToServerCommunications() {
        return clientToServerCommunications;
    }

    @Override
    public MenuOpener getMenuOpener() {
        return menuOpener;
    }

    @Override
    public BucketAmountFormatting getBucketAmountFormatter() {
        return bucketAmountFormatting;
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
