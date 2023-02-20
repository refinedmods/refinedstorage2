package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

public class CraftingGridSourceImpl implements CraftingGridSource {
    private final CraftingGridBlockEntity blockEntity;

    public CraftingGridSourceImpl(final CraftingGridBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public CraftingMatrix getCraftingMatrix() {
        return blockEntity.getCraftingMatrix();
    }

    @Override
    public ResultContainer getCraftingResult() {
        return blockEntity.getCraftingResult();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player) {
        return blockEntity.getRemainingItems(player);
    }

    @Override
    public CraftingGridRefillContext openRefillContext() {
        return new CraftingGridRefillContextImpl(blockEntity);
    }

    @Override
    public CraftingGridRefillContext openSnapshotRefillContext(final Player player) {
        return new SnapshotCraftingGridRefillContext(player, blockEntity, blockEntity.getCraftingMatrix());
    }

    @Override
    public ItemStack insert(final ItemStack stack, final Player player) {
        final GridNetworkNode node = blockEntity.getNode();
        if (!node.isActive()) {
            return stack;
        }
        final Network network = node.getNetwork();
        if (network == null) {
            return stack;
        }
        final long inserted = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM)
            .insert(ItemResource.ofItemStack(stack), stack.getCount(), Action.EXECUTE, new PlayerActor(player));
        final long remainder = stack.getCount() - inserted;
        if (remainder == 0) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount((int) remainder);
    }
}
