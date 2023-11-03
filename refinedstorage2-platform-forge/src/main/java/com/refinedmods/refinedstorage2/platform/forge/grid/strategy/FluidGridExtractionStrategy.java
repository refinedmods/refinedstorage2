package com.refinedmods.refinedstorage2.platform.forge.grid.strategy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.toFluidStack;

public class FluidGridExtractionStrategy implements GridExtractionStrategy {
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final AbstractContainerMenu menu;
    private final GridOperations<FluidResource> gridOperations;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final Storage<ItemResource> itemStorage;

    public FluidGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                       final Player player,
                                       final Grid grid) {
        this.menu = containerMenu;
        this.gridOperations = grid.createOperations(StorageChannelTypes.FLUID, new PlayerActor(player));
        this.playerInventoryStorage = new PlayerMainInvWrapper(player.getInventory());
        this.itemStorage = grid.getItemStorage();
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
                extract(fluidResource, extractMode, cursor, true);
            } else if (bucketInStorageChannel) {
                extract(fluidResource, extractMode, cursor, false);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private IFluidHandlerItem getFluidStorage(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(null);
    }

    private void extract(final FluidResource fluidResource,
                         final GridExtractMode mode,
                         final boolean cursor,
                         final boolean bucketFromInventory) {
        final IFluidHandlerItem destination = getFluidStorage(BUCKET_ITEM_RESOURCE.toItemStack());
        if (destination == null) {
            return; // shouldn't happen
        }
        gridOperations.extract(fluidResource, mode, (resource, amount, action, source) -> {
            final int inserted = destination.fill(toFluidStack(resource, amount), toFluidAction(action));
            if (action == Action.EXECUTE) {
                extractSourceBucket(bucketFromInventory, source);
                if (!insertResultingBucket(cursor, destination)) {
                    insertSourceBucket(bucketFromInventory, source);
                    return 0;
                }
            }
            return inserted;
        });
    }

    private void extractSourceBucket(final boolean bucketFromInventory, final Actor actor) {
        if (bucketFromInventory) {
            extractBucket(playerInventoryStorage, Action.EXECUTE);
        } else {
            itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, actor);
        }
    }

    private void insertSourceBucket(final boolean bucketFromInventory, final Actor actor) {
        if (bucketFromInventory) {
            insertBucket(playerInventoryStorage);
        } else {
            itemStorage.insert(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, actor);
        }
    }

    private boolean insertResultingBucket(final boolean cursor, final IFluidHandlerItem destination) {
        if (cursor) {
            menu.setCarried(destination.getContainer());
            return true;
        } else {
            final ItemStack remainder = ItemHandlerHelper.insertItem(
                playerInventoryStorage,
                destination.getContainer(),
                false
            );
            return remainder.isEmpty();
        }
    }

    private boolean hasBucketInStorage() {
        return itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptyActor.INSTANCE) == 1;
    }

    private boolean hasBucketInInventory() {
        return extractBucket(playerInventoryStorage, Action.SIMULATE);
    }

    private boolean extractBucket(final IItemHandler source, final Action action) {
        final ItemStack toExtractStack = BUCKET_ITEM_RESOURCE.toItemStack();
        for (int slot = 0; slot < source.getSlots(); ++slot) {
            final boolean relevant = isSame(source.getStackInSlot(slot), toExtractStack);
            if (!relevant) {
                continue;
            }
            if (source.extractItem(slot, 1, action == Action.SIMULATE).getCount() == 1) {
                return true;
            }
        }
        return false;
    }

    private void insertBucket(final IItemHandler destination) {
        ItemHandlerHelper.insertItem(destination, BUCKET_ITEM_RESOURCE.toItemStack(), false);
    }

    private boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
