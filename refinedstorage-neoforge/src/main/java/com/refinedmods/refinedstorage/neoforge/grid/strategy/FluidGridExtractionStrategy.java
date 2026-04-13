package com.refinedmods.refinedstorage.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.neoforge.support.resource.SimpleItemStackResourceHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toPlatform;

public class FluidGridExtractionStrategy implements GridExtractionStrategy {
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET);
    private static final net.neoforged.neoforge.transfer.item.ItemResource PLATFORM_BUCKET_ITEM_RESOURCE =
        net.neoforged.neoforge.transfer.item.ItemResource.of(Items.BUCKET);

    private final GridOperations gridOperations;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerInventory;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerCursor;
    private final Storage itemStorage;

    public FluidGridExtractionStrategy(final AbstractContainerMenu containerMenu, final ServerPlayer player,
                                       final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.FLUID, player);
        this.playerInventory = PlayerInventoryWrapper.of(player);
        this.playerCursor = CarriedSlotWrapper.of(containerMenu);
        this.itemStorage = grid.getItemStorage();
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (resource instanceof FluidResource fluidResource) {
            final boolean containerOnCursor = isFluidContainerOnCursor();
            final boolean bucketInInventory = hasBucketInInventory();
            final boolean bucketInStorage = hasBucketInStorage();
            if (containerOnCursor) {
                extractWithContainerOnCursor(fluidResource, extractMode);
            } else if (bucketInInventory) {
                extractWithBucketInInventory(fluidResource, extractMode, cursor);
            } else if (bucketInStorage) {
                extractWithBucketInStorage(fluidResource, extractMode, cursor);
            }
            return true;
        }
        return false;
    }

    private void extractWithContainerOnCursor(final FluidResource fluidResource,
                                              final GridExtractMode mode) {
        try (Transaction tx = Transaction.openRoot()) {
            final ItemStack stack = extractContainerFromCursor(tx);
            if (stack.isEmpty()) {
                return;
            }
            final SimpleItemStackResourceHandler interceptingHandler = SimpleItemStackResourceHandler.forStack(stack);
            final ItemAccess access = ItemAccess.forHandlerIndex(interceptingHandler, 0);
            final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> dest =
                interceptingHandler.getStack().getCapability(Capabilities.Fluid.ITEM, access);
            if (dest == null) {
                return;
            }
            gridOperations.extract(fluidResource, mode, (resource2, amount, action, source) -> {
                if (!(resource2 instanceof FluidResource fluidResource2)) {
                    return 0;
                }
                try (Transaction innerTx = Transaction.open(tx)) {
                    final long inserted = dest.insert(toPlatform(fluidResource2), (int) amount, innerTx);
                    final boolean couldInsertContainer = insertResultingContainerIntoInventory(
                        interceptingHandler,
                        true,
                        innerTx
                    );
                    if (!couldInsertContainer) {
                        return 0;
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

    private ItemStack extractContainerFromCursor(final Transaction tx) {
        final var result = ResourceHandlerUtil.extractFirst(playerCursor, r -> true, 1, tx);
        if (result == null || result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return result.resource().toStack();
    }

    private void extractWithBucketInStorage(final FluidResource fluidResource,
                                            final GridExtractMode mode,
                                            final boolean cursor) {
        final SimpleItemStackResourceHandler interceptingHandler = SimpleItemStackResourceHandler.forEmptyBucket();
        final ItemAccess access = ItemAccess.forHandlerIndex(interceptingHandler, 0);
        final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> destination =
            interceptingHandler.getStack().getCapability(Capabilities.Fluid.ITEM, access);
        if (destination == null) {
            return;
        }
        gridOperations.extract(fluidResource, mode, (resource, amount, action, source) -> {
            if (!(resource instanceof FluidResource fluidResource2)) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                final long inserted = destination.insert(toPlatform(fluidResource2), (int) amount, tx);
                final boolean couldInsertBucket =
                    insertResultingContainerIntoInventory(interceptingHandler, cursor, tx);
                if (!couldInsertBucket) {
                    return 0;
                }
                if (action == Action.EXECUTE) {
                    itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, source);
                    tx.commit();
                }
                return inserted;
            }
        });
    }

    private void extractWithBucketInInventory(final FluidResource fluidResource,
                                              final GridExtractMode mode,
                                              final boolean cursor) {
        try (Transaction tx = Transaction.openRoot()) {
            playerInventory.extract(PLATFORM_BUCKET_ITEM_RESOURCE, 1, tx);
            final SimpleItemStackResourceHandler interceptingHandler = SimpleItemStackResourceHandler.forEmptyBucket();
            final ItemAccess access = ItemAccess.forHandlerIndex(interceptingHandler, 0);
            final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> dest =
                interceptingHandler.getStack().getCapability(Capabilities.Fluid.ITEM, access);
            if (dest == null) {
                return;
            }
            gridOperations.extract(fluidResource, mode, (resource, amount, action, source) -> {
                if (!(resource instanceof FluidResource fluidResource2)) {
                    return 0;
                }
                try (Transaction innerTx = Transaction.open(tx)) {
                    final long inserted = dest.insert(toPlatform(fluidResource2), (int) amount, innerTx);
                    final boolean couldInsertBucket = insertResultingContainerIntoInventory(
                        interceptingHandler,
                        cursor,
                        innerTx
                    );
                    if (!couldInsertBucket) {
                        return 0;
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

    private boolean insertResultingContainerIntoInventory(final SimpleItemStackResourceHandler interceptingHandler,
                                                          final boolean cursor,
                                                          final Transaction innerTx) {
        final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> relevantStorage = cursor
            ? playerCursor
            : playerInventory;
        final net.neoforged.neoforge.transfer.item.ItemResource platformResource =
            net.neoforged.neoforge.transfer.item.ItemResource.of(interceptingHandler.getStack());
        return relevantStorage.insert(platformResource, 1, innerTx) != 0;
    }

    private boolean isFluidContainerOnCursor() {
        final net.neoforged.neoforge.transfer.item.ItemResource platformResource =
            ResourceHandlerUtil.findExtractableResource(playerCursor, r -> true, null);
        if (platformResource == null) {
            return false;
        }
        final ItemStack stack = platformResource.toStack();
        return stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack)) != null;
    }

    private boolean hasBucketInInventory() {
        try (Transaction tx = Transaction.openRoot()) {
            return playerInventory.extract(PLATFORM_BUCKET_ITEM_RESOURCE, 1, tx) == 1;
        }
    }

    private boolean hasBucketInStorage() {
        return itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, Actor.EMPTY) == 1;
    }
}
