package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;

public abstract class AbstractPlatformAbstractions implements PlatformAbstractions {
    private final ServerToClientCommunications serverToClientCommunications;
    private final ClientToServerCommunications clientToServerCommunications;
    private final MenuOpener menuOpener;
    private final BucketQuantityFormatter bucketQuantityFormatter;
    private final FluidRenderer fluidRenderer;

    protected AbstractPlatformAbstractions(ServerToClientCommunications serverToClientCommunications, ClientToServerCommunications clientToServerCommunications, MenuOpener menuOpener, BucketQuantityFormatter bucketQuantityFormatter, FluidRenderer fluidRenderer) {
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
