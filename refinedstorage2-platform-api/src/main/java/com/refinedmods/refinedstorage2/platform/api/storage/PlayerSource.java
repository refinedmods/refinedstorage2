package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.Source;

import net.minecraft.world.entity.player.Player;

public final class PlayerSource implements Source {
    private final String playerName;

    public PlayerSource(final Player player) {
        this(player.getGameProfile().getName());
    }

    public PlayerSource(final String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String getName() {
        return playerName;
    }
}
