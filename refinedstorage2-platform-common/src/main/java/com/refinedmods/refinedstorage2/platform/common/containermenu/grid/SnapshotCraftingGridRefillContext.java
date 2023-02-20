package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SnapshotCraftingGridRefillContext implements CraftingGridRefillContext {
    private final PlayerActor playerActor;
    private final CraftingGridBlockEntity blockEntity;
    private final ResourceList<ItemResource> available = new ResourceListImpl<>();
    private final ResourceList<ItemResource> used = new ResourceListImpl<>();

    public SnapshotCraftingGridRefillContext(
        final Player player,
        final CraftingGridBlockEntity blockEntity,
        final CraftingMatrix craftingMatrix
    ) {
        this.playerActor = new PlayerActor(player);
        this.blockEntity = blockEntity;
        addAvailableItems(craftingMatrix);
    }

    private void addAvailableItems(final CraftingMatrix craftingMatrix) {
        getStorageChannel().ifPresent(storageChannel -> {
            for (int i = 0; i < craftingMatrix.getContainerSize(); ++i) {
                addAvailableItem(craftingMatrix, storageChannel, i);
            }
        });
    }

    private void addAvailableItem(final CraftingMatrix craftingMatrix,
                                  final StorageChannel<ItemResource> storageChannel,
                                  final int craftingMatrixSlotIndex) {
        final ItemStack craftingMatrixStack = craftingMatrix.getItem(craftingMatrixSlotIndex);
        if (craftingMatrixStack.isEmpty()) {
            return;
        }
        addAvailableItem(storageChannel, craftingMatrixStack);
    }

    private void addAvailableItem(final StorageChannel<ItemResource> storageChannel,
                                  final ItemStack craftingMatrixStack) {
        final ItemResource craftingMatrixResource = ItemResource.ofItemStack(craftingMatrixStack);
        // a single resource can occur multiple times in a recipe, only add it once
        if (available.get(craftingMatrixResource).isEmpty()) {
            storageChannel.get(craftingMatrixResource).ifPresent(available::add);
        }
    }

    @Override
    public boolean extract(final ItemResource resource, final Player player) {
        final GridNetworkNode node = blockEntity.getNode();
        if (!node.isActive()) {
            return false;
        }
        final boolean isAvailable = available.get(resource).isPresent();
        if (isAvailable) {
            available.remove(resource, 1);
            used.add(resource, 1);
        }
        return isAvailable;
    }

    @Override
    public void close() {
        getStorageChannel().ifPresent(this::extractUsedItems);
    }

    private void extractUsedItems(final StorageChannel<ItemResource> storageChannel) {
        used.getAll().forEach(u -> storageChannel.extract(u.getResource(), u.getAmount(), Action.EXECUTE, playerActor));
    }

    private Optional<StorageChannel<ItemResource>> getStorageChannel() {
        return Optional.ofNullable(blockEntity.getNode().getNetwork())
            .map(network -> network.getComponent(StorageNetworkComponent.class)
                .getStorageChannel(StorageChannelTypes.ITEM));
    }
}
