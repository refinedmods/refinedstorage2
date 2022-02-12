package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.Source;

import net.minecraft.world.entity.player.Player;

public class PlayerSource implements Source {
    private final Player player;

    public PlayerSource(Player player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
