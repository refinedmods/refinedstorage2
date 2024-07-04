package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractProxyEnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.platform.common.support.energy.CreativeEnergyStorage;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class PortableGridBlockItem extends AbstractEnergyBlockItem {
    private static final Component HELP = createTranslation("item", "portable_grid.help");

    private final PortableGridType type;

    public PortableGridBlockItem(final Block block, final PortableGridType type) {
        super(block, new Item.Properties().stacksTo(1), PlatformApi.INSTANCE.getEnergyItemHelper());
        this.type = type;
    }

    public static PortableGridBlockItemRenderInfo getRenderInfo(final ItemStack stack, final Level level) {
        final boolean creative = isCreative(stack);
        final boolean hasEnergy = creative || createEnergyStorage(stack).getStored() > 0;
        final ItemStack diskStack = getDisk(stack, level.registryAccess());
        final boolean active = hasEnergy && !diskStack.isEmpty();
        final Disk disk = new Disk(
            diskStack.isEmpty() ? null : diskStack.getItem(),
            getState(diskStack, active)
        );
        return new PortableGridBlockItemRenderInfo(active, disk);
    }

    private static boolean isCreative(final ItemStack stack) {
        return stack.getItem() instanceof PortableGridBlockItem portableGridBlockItem
            && portableGridBlockItem.type == PortableGridType.CREATIVE;
    }

    private static StorageState getState(final ItemStack diskStack, final boolean active) {
        if (diskStack.isEmpty() || !(diskStack.getItem() instanceof StorageContainerItem storageContainerItem)) {
            return StorageState.NONE;
        }
        if (!active) {
            return StorageState.INACTIVE;
        }
        final StorageRepository storageRepository = PlatformApi.INSTANCE.getClientStorageRepository();
        return storageContainerItem.getInfo(storageRepository, diskStack)
            .map(storageInfo -> StateTrackedStorage.computeState(storageInfo.capacity(), storageInfo.stored()))
            .orElse(StorageState.INACTIVE);
    }

    private static ItemStack getDisk(final ItemStack stack, final HolderLookup.Provider provider) {
        final CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return ItemStack.EMPTY;
        }
        return AbstractPortableGridBlockEntity.getDisk(blockEntityData, provider);
    }

    static void setDiskInventory(final ItemStack stack,
                                 final DiskInventory diskInventory,
                                 final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        AbstractPortableGridBlockEntity.writeDiskInventory(tag, diskInventory, provider);
        setBlockEntityData(
            stack,
            isCreative(stack)
                ? BlockEntities.INSTANCE.getCreativePortableGrid()
                : BlockEntities.INSTANCE.getPortableGrid(),
            tag
        );
    }

    public static EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity()
        );
        return PlatformApi.INSTANCE.asBlockItemEnergyStorage(
            energyStorage,
            stack,
            BlockEntities.INSTANCE.getPortableGrid()
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final SlotReference slotReference = PlatformApi.INSTANCE.createInventorySlotReference(player, hand);
            final PortableGridEnergyStorage energyStorage = createEnergyStorageInternal(stack);
            final DiskInventoryListenerImpl listener = new DiskInventoryListenerImpl(stack, level.registryAccess());
            final DiskInventory diskInventory = createDiskInventory(stack, listener, level.registryAccess());
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

    private DiskInventory createDiskInventory(final ItemStack stack,
                                              final DiskInventoryListenerImpl listener,
                                              final HolderLookup.Provider provider) {
        final DiskInventory diskInventory = new DiskInventory(listener, 1);
        final CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            AbstractPortableGridBlockEntity.readDiskInventory(customData.copyTag(), diskInventory, provider);
        }
        return diskInventory;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }

    private static class DiskInventoryListenerImpl implements DiskInventory.DiskListener {
        private final ItemStack portableGridStack;
        private final HolderLookup.Provider provider;
        @Nullable
        private PortableGrid portableGrid;

        private DiskInventoryListenerImpl(final ItemStack portableGridStack, final HolderLookup.Provider provider) {
            this.portableGridStack = portableGridStack;
            this.provider = provider;
        }

        @Override
        public void onDiskChanged(final DiskInventory inventory, final int slot) {
            final boolean stillLoading = portableGrid == null;
            if (stillLoading) {
                return;
            }
            setDiskInventory(portableGridStack, inventory, provider);
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
