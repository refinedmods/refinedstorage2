package com.refinedmods.refinedstorage2.platform.common.networking;

import net.minecraft.network.chat.Component;

public record NetworkTransmitterStatus(boolean error, Component message) {
    static NetworkTransmitterStatus error(final Component message) {
        return new NetworkTransmitterStatus(true, message);
    }

    static NetworkTransmitterStatus message(final Component message) {
        return new NetworkTransmitterStatus(false, message);
    }
}
