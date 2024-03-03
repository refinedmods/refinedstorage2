package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BucketPlayerInventoryInsertableStorage implements InsertableStorage {
    private static final ItemStack EMPTY_BUCKET_STACK = new ItemStack(Items.BUCKET);
    private static final ItemResource EMPTY_BUCKET_RESOURCE = ItemResource.ofItemStack(EMPTY_BUCKET_STACK);

    private final Inventory playerInventory;
    private final Storage emptyBucketStorage;
    private final boolean mayDropFilledBucket;

    public BucketPlayerInventoryInsertableStorage(final Inventory playerInventory,
                                                  final Storage emptyBucketStorage,
                                                  final boolean mayDropFilledBucket) {
        this.playerInventory = playerInventory;
        this.emptyBucketStorage = emptyBucketStorage;
        this.mayDropFilledBucket = mayDropFilledBucket;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!(resource instanceof FluidResource fluidResource)) {
            return 0;
        }
        if (amount != Platform.INSTANCE.getBucketAmount()) {
            return 0;
        }
        return Platform.INSTANCE.convertToBucket(fluidResource).map(
            filledBucketStack -> insert(filledBucketStack, amount, action, actor)
        ).orElse(0L);
    }

    private long insert(final ItemStack filledBucketStack, final long amount, final Action action, final Actor actor) {
        if (extractBucketFromInventory(action)) {
            return insert(filledBucketStack, amount, action, this::returnBucketToInventory);
        } else if (extractBucketFromStorage(action, actor)) {
            return insert(filledBucketStack, amount, action, () -> returnBucketToStorage(actor));
        }
        return 0L;
    }

    private long insert(final ItemStack filledBucketStack,
                        final long amount,
                        final Action action,
                        final Runnable rollbackAction) {
        if (action == Action.EXECUTE && !playerInventory.add(filledBucketStack)) {
            if (mayDropFilledBucket) {
                playerInventory.player.drop(filledBucketStack, false);
            } else {
                rollbackAction.run();
                return 0;
            }
        }
        return amount;
    }

    private boolean extractBucketFromInventory(final Action action) {
        for (int i = 0; i < playerInventory.getContainerSize(); ++i) {
            final ItemStack stack = playerInventory.getItem(i);
            if (stack.getItem() == Items.BUCKET) {
                if (action == Action.EXECUTE) {
                    playerInventory.removeItem(i, 1);
                }
                return true;
            }
        }
        return false;
    }

    private void returnBucketToInventory() {
        playerInventory.add(EMPTY_BUCKET_STACK);
    }

    private boolean extractBucketFromStorage(final Action action, final Actor actor) {
        return emptyBucketStorage.extract(EMPTY_BUCKET_RESOURCE, 1, action, actor) == 1;
    }

    private void returnBucketToStorage(final Actor actor) {
        emptyBucketStorage.insert(EMPTY_BUCKET_RESOURCE, 1, Action.EXECUTE, actor);
    }
}
