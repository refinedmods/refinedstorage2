package com.refinedmods.refinedstorage.platform.common.storagemonitor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.platform.api.storagemonitor.StorageMonitorExtractionStrategy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

public class CompositeStorageMonitorExtractionStrategy implements StorageMonitorExtractionStrategy {
    private final List<StorageMonitorExtractionStrategy> strategies = new ArrayList<>();

    public void addStrategy(final StorageMonitorExtractionStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public boolean extract(final ResourceKey resource,
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
