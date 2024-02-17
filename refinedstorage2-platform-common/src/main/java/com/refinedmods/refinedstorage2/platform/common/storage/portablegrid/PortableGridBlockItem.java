package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.AbstractProxyEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.support.energy.ItemEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PortableGridBlockItem extends AbstractEnergyBlockItem {
    private static final String TAG_DISK_INVENTORY = "diskinv";

    private final PortableGridType type;

    public PortableGridBlockItem(final Block block, final PortableGridType type) {
        super(block, new Item.Properties().stacksTo(1), PlatformApi.INSTANCE.getEnergyItemHelper());
        this.type = type;
    }

    public static PortableGridBlockItemRenderInfo getRenderInfo(final ItemStack stack,
                                                                @Nullable final Level level) {
        final boolean creative = stack.getItem() instanceof PortableGridBlockItem portableGridBlockItem
            && portableGridBlockItem.type == PortableGridType.CREATIVE;
        final boolean hasEnergy = creative || ItemEnergyStorage.getStored(stack) > 0;
        final ItemStack diskStack = getDisk(stack);
        final boolean active = hasEnergy && !diskStack.isEmpty();
        final Disk disk = new Disk(
            diskStack.isEmpty() ? null : diskStack.getItem(),
            getState(diskStack, active, level)
        );
        return new PortableGridBlockItemRenderInfo(active, disk);
    }

    private static StorageState getState(final ItemStack diskStack,
                                         final boolean active,
                                         @Nullable final Level level) {
        if (diskStack.isEmpty() || !(diskStack.getItem() instanceof StorageContainerItem storageContainerItem)) {
            return StorageState.NONE;
        }
        if (!active || level == null) {
            return StorageState.INACTIVE;
        }
        final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        return storageContainerItem.getInfo(storageRepository, diskStack)
            .map(storageInfo -> StateTrackedStorage.computeState(storageInfo.capacity(), storageInfo.stored()))
            .orElse(StorageState.INACTIVE);
    }

    private static ItemStack getDisk(final ItemStack stack) {
        if (stack.getTag() == null) {
            return ItemStack.EMPTY;
        }
        if (!stack.getTag().contains(TAG_DISK_INVENTORY)) {
            return ItemStack.EMPTY;
        }
        return ContainerUtil.getItemInSlot(stack.getTag().getCompound(TAG_DISK_INVENTORY), 0);
    }

    static void setDiskInventory(final ItemStack stack, final DiskInventory diskInventory) {
        stack.getOrCreateTag().put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory));
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity()
        );
        return PlatformApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
        final BlockPos pos,
        final Level level,
        @Nullable final Player player,
        final ItemStack stack,
        final BlockState blockState
    ) {
        final boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, blockState);
        if (!level.isClientSide()
            && level.getBlockEntity(pos) instanceof AbstractPortableGridBlockEntity portableGrid) {
            final ItemStack diskStack = getDisk(stack);
            portableGrid.getDiskInventory().setItem(0, diskStack);
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final SlotReference slotReference = PlatformApi.INSTANCE.createInventorySlotReference(player, hand);
            final PortableGridEnergyStorage energyStorage = createEnergyStorageInternal(stack);
            final DiskInventoryListenerImpl listener = new DiskInventoryListenerImpl(stack);
            final DiskInventory diskInventory = createDiskInventory(stack, listener);
            diskInventory.setStorageRepository(PlatformApi.INSTANCE.getStorageRepository(level));
            final PortableGrid portableGrid = new PortableGrid(
                energyStorage,
                diskInventory,
                () -> {
                }
            );
            listener.portableGrid = portableGrid;
            energyStorage.portableGrid = portableGrid;
            portableGrid.updateStorage();
            Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, new PortableGridItemExtendedMenuProvider(
                portableGrid,
                energyStorage,
                diskInventory,
                slotReference
            ));
        }
        return InteractionResultHolder.consume(stack);
    }

    private PortableGridEnergyStorage createEnergyStorageInternal(final ItemStack stack) {
        if (type == PortableGridType.CREATIVE) {
            return new PortableGridEnergyStorage(CreativeEnergyStorage.INSTANCE);
        }
        return new PortableGridEnergyStorage(createEnergyStorage(stack));
    }

    private DiskInventory createDiskInventory(final ItemStack stack, final DiskInventoryListenerImpl listener) {
        final DiskInventory diskInventory = new DiskInventory(listener, 1);
        if (stack.getTag() != null && stack.getTag().contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(stack.getTag().getCompound(TAG_DISK_INVENTORY), diskInventory);
        }
        return diskInventory;
    }

    private static class DiskInventoryListenerImpl implements DiskInventory.DiskListener {
        private final ItemStack portableGridStack;
        @Nullable
        private PortableGrid portableGrid;

        private DiskInventoryListenerImpl(final ItemStack portableGridStack) {
            this.portableGridStack = portableGridStack;
        }

        @Override
        public void onDiskChanged(final DiskInventory inventory, final int slot) {
            final boolean stillLoading = portableGrid == null;
            if (stillLoading) {
                return;
            }
            setDiskInventory(portableGridStack, inventory);
            final boolean wasActive = portableGrid.isGridActive();
            portableGrid.updateStorage();
            final boolean isActive = portableGrid.isGridActive();
            if (wasActive != isActive) {
                portableGrid.activeChanged(isActive);
            }
        }
    }

    private static class PortableGridEnergyStorage extends AbstractProxyEnergyStorage {
        @Nullable
        private PortableGrid portableGrid;

        private PortableGridEnergyStorage(final EnergyStorage energyStorage) {
            super(energyStorage);
        }

        @Override
        public long extract(final long amount, final Action action) {
            if (action == Action.EXECUTE && portableGrid != null) {
                final boolean wasActive = portableGrid.isGridActive();
                final long extracted = super.extract(amount, action);
                final boolean isActive = portableGrid.isGridActive();
                if (wasActive != isActive) {
                    portableGrid.activeChanged(isActive);
                }
                return extracted;
            }
            return super.extract(amount, action);
        }
    }
}
