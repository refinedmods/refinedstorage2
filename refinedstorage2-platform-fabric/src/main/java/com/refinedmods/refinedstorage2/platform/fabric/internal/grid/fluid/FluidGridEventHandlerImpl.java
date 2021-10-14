package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private static final ItemVariant BUCKET_ITEM_VARIANT = ItemVariant.of(Items.BUCKET);
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final ScreenHandler screenHandler;
    private final PlayerEntity player;
    private final GridService<FluidResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final Storage<ItemVariant> playerCursorStorage;
    private final StorageChannel<ItemResource> storageChannel;

    public FluidGridEventHandlerImpl(ScreenHandler screenHandler, GridService<FluidResource> gridService, PlayerInventory playerInventory, StorageChannel<ItemResource> storageChannel) {
        this.screenHandler = screenHandler;
        this.player = playerInventory.player;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(screenHandler);
        this.storageChannel = storageChannel;
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
        FluidResource fluidResource = new FluidResource(extractableResource.getFluid(), extractableResource.getNbt());
        gridService.insert(fluidResource, insertMode, (resource, amount, action) -> {
            FluidVariant fluidVariant = resource.getFluidVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = cursorStorage.extract(fluidVariant, amount, tx);
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
                screenHandler.getCursorStack(),
                ContainerItemContext.ofPlayerCursor(player, screenHandler)
        );
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        boolean bucketInInventory = hasBucketInInventory();
        boolean bucketInStorageChannel = storageChannel.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE) == 1;
        if (bucketInInventory) {
            extractWithBucketInInventory(fluidResource, mode, cursor);
        } else if (bucketInStorageChannel) {
            // TODO extractWithBucketInStorageChannel(fluidResource, mode, cursor);
        }
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
                    long inserted = destination.insert(FluidVariant.of(resource.getFluid(), resource.getTag()), amount, innerTx);
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
        return insert(ItemVariant.of(interceptingStorage.getStack()), 1, innerTx, cursor ? playerCursorStorage : playerInventoryStorage) != 0;
    }

    private boolean hasBucketInInventory() {
        try (Transaction tx = Transaction.openOuter()) {
            return playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx) == 1;
        }
    }

    // TODO: remove when upgrading Fabric
    private long insert(ItemVariant itemVariant, long amount, Transaction tx, Storage<ItemVariant> targetStorage) {
        if (targetStorage instanceof PlayerInventoryStorage) {
            return ((PlayerInventoryStorage) targetStorage).offer(itemVariant, amount, tx);
        }
        return targetStorage.insert(itemVariant, amount, tx);
    }
}
