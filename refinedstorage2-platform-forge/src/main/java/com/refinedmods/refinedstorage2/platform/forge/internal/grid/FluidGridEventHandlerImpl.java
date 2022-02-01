package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET, null);

    private final AbstractContainerMenu screenHandler;
    private final Inventory playerInventory;
    private final GridService<FluidResource> gridService;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final ExtractableStorage<ItemResource> bucketStorage;

    public FluidGridEventHandlerImpl(AbstractContainerMenu screenHandler, Inventory playerInventory, GridService<FluidResource> gridService, ExtractableStorage<ItemResource> bucketStorage) {
        this.screenHandler = screenHandler;
        this.playerInventory = playerInventory;
        this.gridService = gridService;
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.bucketStorage = bucketStorage;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        IFluidHandlerItem cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        FluidStack extractableResource = cursorStorage.getFluidInTank(0);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action) -> {
            FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            FluidStack drained = cursorStorage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                screenHandler.setCarried(cursorStorage.getContainer());
            }
            return drained.getAmount();
        });
    }

    private IFluidHandler.FluidAction toFluidAction(Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }

    @Nullable
    private IFluidHandlerItem getFluidCursorStorage() {
        return getFluidStorage(screenHandler.getCarried());
    }

    @Nullable
    private IFluidHandlerItem getFluidStorage(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
    }

    @Override
    public void onTransfer(int slotIndex) {
        IFluidHandlerItem storage = getFluidStorage(playerInventory.getItem(slotIndex));
        if (storage == null) {
            return;
        }
        FluidStack extractableResource = storage.getFluidInTank(0);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            FluidStack drained = storage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                playerInventory.setItem(slotIndex, storage.getContainer());
            }
            return drained.getAmount();
        });
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        boolean bucketInInventory = hasBucketInInventory();
        boolean bucketInStorageChannel = hasBucketInStorage();
        if (bucketInInventory) {
            extractBucket(fluidResource, mode, cursor, true);
        } else if (bucketInStorageChannel) {
            extractBucket(fluidResource, mode, cursor, false);
        }
    }

    private void extractBucket(FluidResource fluidResource, GridExtractMode mode, boolean cursor, boolean bucketFromInventory) {
        IFluidHandlerItem destination = getFluidStorage(toItemStack(BUCKET_ITEM_RESOURCE, 1));
        if (destination == null) {
            return; // shouldn't happen
        }
        gridService.extract(fluidResource, mode, (resource, amount, action) -> {
            int inserted = destination.fill(toFluidStack(resource, amount), toFluidAction(action));
            boolean couldInsertBucket = canInsertResultingBucketIntoInventory(destination, cursor);
            if (!couldInsertBucket) {
                return amount;
            }
            long remainder = amount - inserted;
            if (action == Action.EXECUTE) {
                extractSourceBucketFromInventory(bucketFromInventory);
                insertResultingBucketIntoInventory(cursor, destination);
            }
            return remainder;
        });
    }

    private void extractSourceBucketFromInventory(boolean bucketFromInventory) {
        if (bucketFromInventory) {
            extractBucket(playerInventoryStorage, Action.EXECUTE);
        } else {
            bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE);
        }
    }

    private boolean canInsertResultingBucketIntoInventory(IFluidHandlerItem storage, boolean cursor) {
        if (cursor) {
            return screenHandler.getCarried().isEmpty();
        }
        return ItemHandlerHelper.insertItem(playerInventoryStorage, storage.getContainer(), true).isEmpty();
    }

    private void insertResultingBucketIntoInventory(boolean cursor, IFluidHandlerItem destination) {
        if (cursor) {
            screenHandler.setCarried(destination.getContainer());
        } else {
            ItemHandlerHelper.insertItem(playerInventoryStorage, destination.getContainer(), false);
        }
    }

    private boolean hasBucketInStorage() {
        return bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE) == 1;
    }

    private boolean hasBucketInInventory() {
        return extractBucket(playerInventoryStorage, Action.SIMULATE);
    }

    private boolean extractBucket(IItemHandler source, Action action) {
        ItemStack toExtractStack = toItemStack(BUCKET_ITEM_RESOURCE, 1);
        for (int slot = 0; slot < source.getSlots(); ++slot) {
            boolean relevant = isSame(source.getStackInSlot(slot), toExtractStack);
            if (!relevant) {
                continue;
            }
            if (source.extractItem(slot, 1, action == Action.SIMULATE).getCount() == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isSame(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
