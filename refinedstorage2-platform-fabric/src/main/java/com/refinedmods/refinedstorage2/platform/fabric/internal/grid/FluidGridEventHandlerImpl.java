package com.refinedmods.refinedstorage2.platform.fabric.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private static final ItemVariant BUCKET_ITEM_VARIANT = ItemVariant.of(Items.BUCKET);
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final AbstractContainerMenu menu;
    private final Player player;
    private final GridService<FluidResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final Storage<ItemVariant> playerCursorStorage;
    private final ExtractableStorage<ItemResource> bucketStorage;

    public FluidGridEventHandlerImpl(final AbstractContainerMenu menu,
                                     final GridService<FluidResource> gridService,
                                     final Inventory playerInventory,
                                     final ExtractableStorage<ItemResource> bucketStorage) {
        this.menu = menu;
        this.player = playerInventory.player;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(menu);
        this.bucketStorage = bucketStorage;
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        final Storage<FluidVariant> cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        final FluidVariant extractableResource = StorageUtil.findExtractableResource(cursorStorage, null);
        if (extractableResource == null) {
            return;
        }
        final FluidResource fluidResource = ofFluidVariant(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action, source) -> {
            final FluidVariant fluidVariant = toFluidVariant(resource);
            try (Transaction tx = Transaction.openOuter()) {
                final long extracted = cursorStorage.extract(fluidVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onTransfer(final int slotIndex) {
        final SingleSlotStorage<ItemVariant> itemSlotStorage = playerInventoryStorage.getSlot(slotIndex);
        if (itemSlotStorage == null) {
            return;
        }
        final Storage<FluidVariant> fluidSlotStorage = FluidStorage.ITEM.find(
                itemSlotStorage.getResource().toStack(),
                ContainerItemContext.ofPlayerSlot(player, itemSlotStorage)
        );
        if (fluidSlotStorage == null) {
            return;
        }
        final FluidVariant extractableResource = StorageUtil.findExtractableResource(fluidSlotStorage, null);
        if (extractableResource == null) {
            return;
        }
        final FluidResource fluidResource = ofFluidVariant(extractableResource);
        gridService.insert(fluidResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action, source) -> {
            final FluidVariant fluidVariant = toFluidVariant(resource);
            try (Transaction tx = Transaction.openOuter()) {
                final long extracted = fluidSlotStorage.extract(fluidVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Nullable
    private Storage<FluidVariant> getFluidCursorStorage() {
        return FluidStorage.ITEM.find(
                menu.getCarried(),
                ContainerItemContext.ofPlayerCursor(player, menu)
        );
    }

    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        final boolean bucketInInventory = hasBucketInInventory();
        final boolean bucketInStorageChannel = hasBucketInStorage();
        if (bucketInInventory) {
            extractWithBucketInInventory(fluidResource, mode, cursor);
        } else if (bucketInStorageChannel) {
            extractWithBucketInStorage(fluidResource, mode, cursor);
        }
    }

    private void extractWithBucketInStorage(final FluidResource fluidResource,
                                            final GridExtractMode mode,
                                            final boolean cursor) {
        final FluidGridExtractionInterceptingStorage interceptingStorage = new FluidGridExtractionInterceptingStorage();
        final Storage<FluidVariant> destination = FluidStorage.ITEM.find(
                interceptingStorage.getStack(),
                ContainerItemContext.ofSingleSlot(interceptingStorage)
        );
        if (destination == null) {
            return;
        }
        gridService.extract(fluidResource, mode, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final long inserted = destination.insert(toFluidVariant(resource), amount, tx);
                final boolean couldInsertBucket = insertResultingBucketIntoInventory(interceptingStorage, cursor, tx);
                if (!couldInsertBucket) {
                    return amount;
                }
                if (action == Action.EXECUTE) {
                    bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, source);
                    tx.commit();
                }
                return inserted;
            }
        });
    }

    private void extractWithBucketInInventory(final FluidResource fluidResource,
                                              final GridExtractMode mode,
                                              final boolean cursor) {
        try (Transaction tx = Transaction.openOuter()) {
            playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx);
            final FluidGridExtractionInterceptingStorage interceptingStorage
                    = new FluidGridExtractionInterceptingStorage();
            final Storage<FluidVariant> destination = FluidStorage.ITEM.find(
                    interceptingStorage.getStack(),
                    ContainerItemContext.ofSingleSlot(interceptingStorage)
            );
            if (destination == null) {
                return;
            }
            gridService.extract(fluidResource, mode, (resource, amount, action, source) -> {
                try (Transaction innerTx = tx.openNested()) {
                    final long inserted = destination.insert(toFluidVariant(resource), amount, innerTx);
                    final boolean couldInsertBucket = insertResultingBucketIntoInventory(
                            interceptingStorage,
                            cursor,
                            innerTx
                    );
                    if (!couldInsertBucket) {
                        return amount;
                    }
                    if (action == Action.EXECUTE) {
                        innerTx.commit();
                        tx.commit();
                    }
                    return inserted;
                }
            });
        }
    }

    private boolean insertResultingBucketIntoInventory(final FluidGridExtractionInterceptingStorage interceptingStorage,
                                                       final boolean cursor,
                                                       final Transaction innerTx) {
        final Storage<ItemVariant> relevantStorage = cursor ? playerCursorStorage : playerInventoryStorage;
        final ItemVariant itemVariant = ItemVariant.of(interceptingStorage.getStack());
        return relevantStorage.insert(itemVariant, 1, innerTx) != 0;
    }

    private boolean hasBucketInInventory() {
        try (Transaction tx = Transaction.openOuter()) {
            return playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx) == 1;
        }
    }

    private boolean hasBucketInStorage() {
        return bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptySource.INSTANCE) == 1;
    }
}
