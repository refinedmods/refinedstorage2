package com.refinedmods.refinedstorage2.platform.fabric.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.ofItemVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toItemVariant;

public class ItemGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridService<ItemResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final PlatformGridServiceFactory gridServiceFactory) {
        this.containerMenu = containerMenu;
        this.gridService = gridServiceFactory.createForItem(new PlayerActor(player));
        this.playerInventoryStorage = PlayerInventoryStorage.of(player.getInventory());
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridService.insert(itemResource, insertMode, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = playerCursorStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        final SingleSlotStorage<ItemVariant> storage = playerInventoryStorage.getSlot(slotIndex);
        final ItemVariant itemVariantInSlot = StorageUtil.findExtractableResource(storage, null);
        if (itemVariantInSlot == null) {
            return false;
        }
        final ItemResource itemResource = ofItemVariant(itemVariantInSlot);
        gridService.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = storage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
        return true;
    }
}
