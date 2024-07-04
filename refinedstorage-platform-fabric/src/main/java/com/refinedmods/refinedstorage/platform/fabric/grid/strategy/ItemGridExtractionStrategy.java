package com.refinedmods.refinedstorage.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceTypes;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.platform.fabric.support.resource.VariantUtil.toItemVariant;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridOperations gridOperations;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final ServerPlayer player,
                                      final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerInventoryStorage = PlayerInventoryStorage.of(player.getInventory());
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (resource instanceof ItemResource itemResource) {
            gridOperations.extract(itemResource, extractMode, (r, amount, action, source) -> {
                if (!(r instanceof ItemResource itemResource2)) {
                    return 0;
                }
                final ItemVariant itemVariant = toItemVariant(itemResource2);
                try (Transaction tx = Transaction.openOuter()) {
                    final long inserted = insert(itemVariant, amount, tx, cursor);
                    if (action == Action.EXECUTE) {
                        tx.commit();
                    }
                    return inserted;
                }
            });
            return true;
        }
        return false;
    }

    private long insert(final ItemVariant itemVariant, final long amount, final Transaction tx, final boolean cursor) {
        final Storage<ItemVariant> relevantStorage = cursor ? playerCursorStorage : playerInventoryStorage;
        return relevantStorage.insert(itemVariant, amount, tx);
    }
}
