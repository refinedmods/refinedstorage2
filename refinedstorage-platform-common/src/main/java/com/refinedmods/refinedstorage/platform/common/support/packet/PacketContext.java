package com.refinedmods.refinedstorage.platform.common.support.packet;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PacketContext {
    Player getPlayer();
}
