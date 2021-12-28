package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;

public final class PlatformAbstractions {
    public static final PlatformAbstractions INSTANCE = new PlatformAbstractions();

    private ServerToClientCommunications serverToClientCommunications;
    private ClientToServerCommunications clientToServerCommunications;
    private MenuOpener menuOpener;
    private long bucketAmount;

    private PlatformAbstractions() {
    }

    public ServerToClientCommunications getServerToClientCommunications() {
        return serverToClientCommunications;
    }

    public void setServerToClientCommunications(ServerToClientCommunications serverToClientCommunications) {
        this.serverToClientCommunications = serverToClientCommunications;
    }

    public ClientToServerCommunications getClientToServerCommunications() {
        return clientToServerCommunications;
    }

    public void setClientToServerCommunications(ClientToServerCommunications clientToServerCommunications) {
        this.clientToServerCommunications = clientToServerCommunications;
    }

    public MenuOpener getMenuOpener() {
        return menuOpener;
    }

    public void setMenuOpener(MenuOpener menuOpener) {
        this.menuOpener = menuOpener;
    }

    public long getBucketAmount() {
        return bucketAmount;
    }

    public void setBucketAmount(long bucketAmount) {
        this.bucketAmount = bucketAmount;
    }
}
