package com.refinedmods.refinedstorage2.platform.common.block.entity.storagemonitor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.blockentity.storagemonitor.StorageMonitorExtractionStrategy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

public class CompositeStorageMonitorExtractionStrategy implements StorageMonitorExtractionStrategy {
    private final List<StorageMonitorExtractionStrategy> strategies = new ArrayList<>();

    public void addStrategy(final StorageMonitorExtractionStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public boolean extract(final Object resource,
                           final boolean fullStack,
                           final Player player,
                           final Actor actor,
                           final Network network) {
        for (final StorageMonitorExtractionStrategy strategy : strategies) {
            if (strategy.extract(resource, fullStack, player, actor, network)) {
                return true;
            }
        }
        return false;
    }
}
