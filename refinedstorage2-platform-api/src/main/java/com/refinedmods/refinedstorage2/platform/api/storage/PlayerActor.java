package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.Actor;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public final class PlayerActor implements Actor {
    private final String playerName;

    public PlayerActor(final Player player) {
        this(player.getGameProfile().getName());
    }

    public PlayerActor(final String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String getName() {
        return playerName;
    }
}
