package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.api.storage.Actor;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public record PlayerActor(String name) implements Actor {
    public PlayerActor(final Player player) {
        this(player.getGameProfile().name());
    }

    @Override
    public String getName() {
        return name;
    }
}
