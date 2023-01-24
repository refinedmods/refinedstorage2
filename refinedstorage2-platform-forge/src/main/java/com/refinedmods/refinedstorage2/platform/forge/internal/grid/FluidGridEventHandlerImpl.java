package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.FluidGridEventHandler;

import javax.annotation.Nullable;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

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

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private IFluidHandlerItem getFluidStorage(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(null);
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
        final IFluidHandlerItem destination = getFluidStorage(BUCKET_ITEM_RESOURCE.toItemStack());
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

    private void extractSourceBucket(final boolean bucketFromInventory, final Actor actor) {
        if (bucketFromInventory) {
            extractBucket(playerInventoryStorage, Action.EXECUTE);
        } else {
            bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, actor);
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
        return bucketStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptyActor.INSTANCE) == 1;
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

    private boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
