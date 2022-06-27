package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.common.util.BucketQuantityFormatter;

public abstract class AbstractPlatform implements Platform {
    private final ServerToClientCommunications serverToClientCommunications;
    private final ClientToServerCommunications clientToServerCommunications;
    private final MenuOpener menuOpener;
    private final BucketQuantityFormatter bucketQuantityFormatter;
    private final FluidRenderer fluidRenderer;

    protected AbstractPlatform(final ServerToClientCommunications serverToClientCommunications,
                               final ClientToServerCommunications clientToServerCommunications,
                               final MenuOpener menuOpener,
                               final BucketQuantityFormatter bucketQuantityFormatter,
                               final FluidRenderer fluidRenderer) {
        this.serverToClientCommunications = serverToClientCommunications;
        this.clientToServerCommunications = clientToServerCommunications;
        this.menuOpener = menuOpener;
        this.bucketQuantityFormatter = bucketQuantityFormatter;
        this.fluidRenderer = fluidRenderer;
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
    public BucketQuantityFormatter getBucketQuantityFormatter() {
        return bucketQuantityFormatter;
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return fluidRenderer;
    }
}
