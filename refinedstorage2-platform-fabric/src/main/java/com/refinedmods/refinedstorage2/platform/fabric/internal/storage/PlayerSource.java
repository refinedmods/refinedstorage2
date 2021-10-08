package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.Source;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerSource implements Source {
    private final PlayerEntity player;

    public PlayerSource(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
