package com.refinedmods.refinedstorage.platform.common.support.packet;

@FunctionalInterface
public interface PacketHandler<T> {
    void handle(T packet, PacketContext ctx);
}
