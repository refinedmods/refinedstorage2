package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import javax.annotation.Nullable;

import net.minecraft.world.Containers;
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

    private final AbstractContainerMenu menu;
    private final Inventory playerInventory;
    private final GridService<FluidResource> gridService;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final ExtractableStorage<ItemResource> bucketStorage;

    public FluidGridEventHandlerImpl(final AbstractContainerMenu menu,
                                     final Inventory playerInventory,
                                     final GridService<FluidResource> gridService,
                                     final ExtractableStorage<ItemResource> bucketStorage) {
        this.menu = menu;
        this.playerInventory = playerInventory;
        this.gridService = gridService;
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.bucketStorage = bucketStorage;
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        final IFluidHandlerItem cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        final FluidStack extractableResource = cursorStorage.getFluidInTank(0);
        if (extractableResource.isEmpty()) {
            return;
        }
        final FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action, source) -> {
            final FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            final FluidStack drained = cursorStorage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                menu.setCarried(cursorStorage.getContainer());
            }
            return drained.getAmount();
        });
    }

    private IFluidHandler.FluidAction toFluidAction(final Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }

    @Nullable
    private IFluidHandlerItem getFluidCursorStorage() {
        return getFluidStorage(menu.getCarried());
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private IFluidHandlerItem getFluidStorage(final ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
    }

    @Override
    public void onTransfer(final int slotIndex) {
        final IFluidHandlerItem storage = getFluidStorage(playerInventory.getItem(slotIndex));
        if (storage == null) {
            return;
        }
        final FluidStack extractableResource = storage.getFluidInTank(0);
        if (extractableResource.isEmpty()) {
            return;
        }
        final FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action, source) -> {
            final FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            final FluidStack drained = storage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                playerInventory.setItem(slotIndex, storage.getContainer());
            }
            return drained.getAmount();
        });
    }

    @Override
    public void onExtract(final FluidResource fluidResource, final GridExtractMode mode, final boolean cursor) {
        final boolean bucketInInventory = hasBucketInInventory();
        final boolean bucketInStorageChannel = hasBucketInStorage();
        if (bucketInInventory) {
            extract(fluidResource, mode, cursor, true);
        } else if (bucketInStorageChannel) {
            extract(fluidResource, mode, cursor, false);
        }
    }

    private void extract(final FluidResource fluidResource,
                         final GridExtractMode mode,
                         final boolean cursor,
                         final boolean bucketFromInventory) {
        final IFluidHandlerItem destination = getFluidStorage(toItemStack(BUCKET_ITEM_RESOURCE, 1));
        if (destination == null) {
            return; // shouldn't happen
        }
        gridService.extract(fluidResource, mode, (resource, amount, action, source) -> {
            final int inserted = destination.fill(toFluidStack(resource, amount), toFluidAction(action));
            if (action == Action.EXECUTE) {
                extractSourceBucket(bucketFromInventory, source);
                insertResultingBucket(cursor, destination);
            }
            return inserted;
        });
    }

    private void extractSourceBucket(final boolean bucketFromInventory, final Source source) {
        if (bucketFromInventory) {
            extractBucket(playerInventoryStorage, Action.EXECUTE);
        } else {
            bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, source);
        }
    }

    private void insertResultingBucket(final boolean cursor, final IFluidHandlerItem destination) {
        if (cursor) {
            menu.setCarried(destination.getContainer());
        } else {
            final ItemStack remainder = ItemHandlerHelper.insertItem(
                    playerInventoryStorage,
                    destination.getContainer(),
                    false
            );
            if (!remainder.isEmpty()) {
                // TODO: This isn't ideal, but dealing without transactions on the inventory doesn't make it easy.
                Containers.dropItemStack(
                        playerInventory.player.getCommandSenderWorld(),
                        playerInventory.player.getX(),
                        playerInventory.player.getY(),
                        playerInventory.player.getZ(),
                        destination.getContainer()
                );
            }
        }
    }

    private boolean hasBucketInStorage() {
        return bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptySource.INSTANCE) == 1;
    }

    private boolean hasBucketInInventory() {
        return extractBucket(playerInventoryStorage, Action.SIMULATE);
    }

    private boolean extractBucket(final IItemHandler source, final Action action) {
        final ItemStack toExtractStack = toItemStack(BUCKET_ITEM_RESOURCE, 1);
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

    private boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
