package com.refinedmods.refinedstorage2.platform.fabric.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class FluidGridExtractionStrategy implements GridExtractionStrategy {
    private static final ItemVariant BUCKET_ITEM_VARIANT = ItemVariant.of(Items.BUCKET);
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final GridService<FluidResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final Storage<ItemVariant> playerCursorStorage;
    private final ExtractableStorage<ItemResource> containerExtractionSource;

    public FluidGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                       final Player player,
                                       final PlatformGridServiceFactory gridServiceFactory,
                                       final ExtractableStorage<ItemResource> containerExtractionSource) {
        this.gridService = gridServiceFactory.createForFluid(new PlayerActor(player));
        this.playerInventoryStorage = PlayerInventoryStorage.of(player.getInventory());
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
        this.containerExtractionSource = containerExtractionSource;
    }

    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
                                 final GridExtractMode extractMode,
                                 final boolean cursor) {
        if (resource instanceof FluidResource fluidResource) {
            final boolean bucketInInventory = hasBucketInInventory();
            final boolean bucketInStorageChannel = hasBucketInStorage();
            if (bucketInInventory) {
                extractWithBucketInInventory(fluidResource, extractMode, cursor);
            } else if (bucketInStorageChannel) {
                extractWithBucketInStorage(fluidResource, extractMode, cursor);
            }
            return true;
        }
        return false;
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
                    containerExtractionSource.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, source);
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
        return containerExtractionSource.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptyActor.INSTANCE) == 1;
    }
}
