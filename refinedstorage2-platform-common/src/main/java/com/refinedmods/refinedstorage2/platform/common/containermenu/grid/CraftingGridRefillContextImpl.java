package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.world.entity.player.Player;

public class CraftingGridRefillContextImpl implements CraftingGridRefillContext {
    private final CraftingGridBlockEntity blockEntity;

    public CraftingGridRefillContextImpl(final CraftingGridBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean extract(final ItemResource resource, final Player player) {
        final GridNetworkNode node = blockEntity.getNode();
        if (!node.isActive()) {
            return false;
        }
        final Network network = node.getNetwork();
        if (network == null) {
            return false;
        }
        return network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM)
            .extract(resource, 1, Action.EXECUTE, new PlayerActor(player)) == 1;
    }

    @Override
    public void close() {
        // no op
    }
}
