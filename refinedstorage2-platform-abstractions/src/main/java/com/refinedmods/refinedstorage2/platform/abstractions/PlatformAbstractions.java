package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;

public final class PlatformAbstractions {
    public static final PlatformAbstractions INSTANCE = new PlatformAbstractions();

    private ServerToClientCommunications serverToClientCommunications;
    private ClientToServerCommunications clientToServerCommunications;

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
}
