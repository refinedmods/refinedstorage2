package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

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

import static com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource.ofFluidVariant;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private static final ItemVariant BUCKET_ITEM_VARIANT = ItemVariant.of(Items.BUCKET);
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final AbstractContainerMenu screenHandler;
    private final Player player;
    private final GridService<FluidResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final Storage<ItemVariant> playerCursorStorage;
    private final ExtractableStorage<ItemResource> bucketStorage;

    public FluidGridEventHandlerImpl(AbstractContainerMenu screenHandler, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage) {
        this.screenHandler = screenHandler;
        this.player = playerInventory.player;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(screenHandler);
        this.bucketStorage = bucketStorage;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        Storage<FluidVariant> cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        FluidVariant extractableResource = StorageUtil.findExtractableResource(cursorStorage, null);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidVariant(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action) -> {
            FluidVariant fluidVariant = resource.toFluidVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = cursorStorage.extract(fluidVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onTransfer(int slotIndex) {
        SingleSlotStorage<ItemVariant> itemSlotStorage = playerInventoryStorage.getSlot(slotIndex);
        if (itemSlotStorage == null) {
            return;
        }
        Storage<FluidVariant> fluidSlotStorage = FluidStorage.ITEM.find(itemSlotStorage.getResource().toStack(), ContainerItemContext.ofPlayerSlot(player, itemSlotStorage));
        if (fluidSlotStorage == null) {
            return;
        }
        FluidVariant extractableResource = StorageUtil.findExtractableResource(fluidSlotStorage, null);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidVariant(extractableResource);
        gridService.insert(fluidResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            FluidVariant fluidVariant = resource.toFluidVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = fluidSlotStorage.extract(fluidVariant, amount, tx);
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
                screenHandler.getCarried(),
                ContainerItemContext.ofPlayerCursor(player, screenHandler)
        );
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        boolean bucketInInventory = hasBucketInInventory();
        boolean bucketInStorageChannel = hasBucketInStorage();
        if (bucketInInventory) {
            extractWithBucketInInventory(fluidResource, mode, cursor);
        } else if (bucketInStorageChannel) {
            extractWithBucketInStorage(fluidResource, mode, cursor);
        }
    }

    private void extractWithBucketInStorage(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        FluidGridExtractionInterceptingStorage interceptingStorage = new FluidGridExtractionInterceptingStorage();
        Storage<FluidVariant> destination = FluidStorage.ITEM.find(
                interceptingStorage.getStack(),
                ContainerItemContext.ofSingleSlot(interceptingStorage)
        );
        if (destination == null) {
            return;
        }
        gridService.extract(fluidResource, mode, (resource, amount, action) -> {
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = destination.insert(resource.toFluidVariant(), amount, tx);
                boolean couldInsertBucket = insertResultingBucketIntoInventory(interceptingStorage, cursor, tx);
                if (!couldInsertBucket) {
                    return amount;
                }
                long remainder = amount - inserted;
                if (action == Action.EXECUTE) {
                    bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE);
                    tx.commit();
                }
                return remainder;
            }
        });
    }

    private void extractWithBucketInInventory(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        try (Transaction tx = Transaction.openOuter()) {
            playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx);
            FluidGridExtractionInterceptingStorage interceptingStorage = new FluidGridExtractionInterceptingStorage();
            Storage<FluidVariant> destination = FluidStorage.ITEM.find(
                    interceptingStorage.getStack(),
                    ContainerItemContext.ofSingleSlot(interceptingStorage)
            );
            if (destination == null) {
                return;
            }
            gridService.extract(fluidResource, mode, (resource, amount, action) -> {
                try (Transaction innerTx = tx.openNested()) {
                    long inserted = destination.insert(resource.toFluidVariant(), amount, innerTx);
                    boolean couldInsertBucket = insertResultingBucketIntoInventory(interceptingStorage, cursor, innerTx);
                    if (!couldInsertBucket) {
                        return amount;
                    }
                    long remainder = amount - inserted;
                    if (action == Action.EXECUTE) {
                        innerTx.commit();
                        tx.commit();
                    }
                    return remainder;
                }
            });
        }
    }

    private boolean insertResultingBucketIntoInventory(FluidGridExtractionInterceptingStorage interceptingStorage, boolean cursor, Transaction innerTx) {
        Storage<ItemVariant> relevantStorage = cursor ? playerCursorStorage : playerInventoryStorage;
        ItemVariant itemVariant = ItemVariant.of(interceptingStorage.getStack());
        return relevantStorage.insert(itemVariant, 1, innerTx) != 0;
    }

    private boolean hasBucketInInventory() {
        try (Transaction tx = Transaction.openOuter()) {
            return playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx) == 1;
        }
    }

    private boolean hasBucketInStorage() {
        return bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE) == 1;
    }
}
