package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractProxyEnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.UsablePlayerSlotReferencedItem;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.support.slotreference.PlayerSlotReferenceEnergyItemContext;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNullElse;

public class PortableGridBlockItem extends AbstractEnergyBlockItem implements UsablePlayerSlotReferencedItem {
    private static final Component HELP = createTranslation("item", "portable_grid.help");

    private final PortableGridType type;

    public PortableGridBlockItem(final Block block, final PortableGridType type) {
        super(block, new Item.Properties()
                .stacksTo(1)
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM, type == PortableGridType.CREATIVE
                    ? ContentIds.CREATIVE_PORTABLE_GRID
                    : ContentIds.PORTABLE_GRID)),
            RefinedStorageApi.INSTANCE.getEnergyItemHelper());
        this.type = type;
    }

    public static PortableGridBlockItemRenderInfo getRenderInfo(final ItemStack stack) {
        final boolean creative = isCreative(stack);
        final boolean hasEnergy = creative
            || !RefinedStorageApi.INSTANCE.isEnergyRequired()
            || getStored(stack) > 0;
        final ItemStack diskStack = getDisk(stack);
        final boolean active = hasEnergy && !diskStack.isEmpty();
        final Disk disk = new Disk(
            diskStack.isEmpty() ? null : diskStack.getItem(),
            getState(diskStack, active)
        );
        return new PortableGridBlockItemRenderInfo(active, disk);
    }

    private static long getStored(final ItemStack stack) {
        return RefinedStorageApi.INSTANCE.getEnergyStorage(stack, EnergyItemContext.READONLY)
            .map(EnergyStorage::getStored).orElse(0L);
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
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getClientStorageRepository();
        return storageContainerItem.getInfo(storageRepository, diskStack)
            .map(storageInfo -> StateTrackedStorage.computeState(storageInfo.capacity(), storageInfo.stored()))
            .orElse(StorageState.INACTIVE);
    }

    private static ItemStack getDisk(final ItemStack stack) {
        final TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return ItemStack.EMPTY;
        }
        return AbstractPortableGridBlockEntity.getDisk(blockEntityData.copyTagWithoutId());
    }

    static void setDiskInventory(final ItemStack stack,
                                 final DiskInventory diskInventory,
                                 final HolderLookup.Provider registries) {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        AbstractPortableGridBlockEntity.writeDiskInventory(output, diskInventory);
        ItemBlockEnergyStorage.store(output, getStored(stack));
        setBlockEntityData(stack, isCreative(stack)
            ? BlockEntities.INSTANCE.getCreativePortableGrid()
            : BlockEntities.INSTANCE.getPortableGrid(), output);
    }

    public static EnergyStorage createEnergyStorage(final ItemStack stack, final EnergyItemContext context) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Math.clamp(Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity(), 1, Long.MAX_VALUE)
        );
        return RefinedStorageApi.INSTANCE.createBlockItemEnergyStorage(
            energyStorage,
            stack,
            BlockEntities.INSTANCE.getPortableGrid(),
            context
        );
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (player.isCrouching()) {
            return super.useOn(context);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            final PlayerSlotReference playerSlotReference = RefinedStorageApi.INSTANCE
                .createPlayerInventorySlotReference(player, context.getHand());
            final ItemStack stack = playerSlotReference.get(player);
            use(serverPlayer, stack, playerSlotReference);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final PlayerSlotReference playerSlotReference = RefinedStorageApi.INSTANCE
                .createPlayerInventorySlotReference(player, hand);
            use(serverPlayer, stack, playerSlotReference);
        }
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }

    @Override
    public void use(final ServerPlayer player, final ItemStack stack, final PlayerSlotReference playerSlotReference) {
        final PortableGridEnergyStorage energyStorage = createEnergyStorageInternal(stack,
            new PlayerSlotReferenceEnergyItemContext(player, playerSlotReference));
        final Level level = player.level();
        final DiskInventoryListener listener = new DiskInventoryListener(stack, level.registryAccess());
        final DiskInventory diskInventory = createDiskInventory(stack, listener, level.registryAccess());
        diskInventory.setStorageRepository(RefinedStorageApi.INSTANCE.getStorageRepository(level));
        final PortableGrid portableGrid = new PortableGrid(energyStorage, diskInventory, () -> {
        });
        listener.portableGrid = portableGrid;
        energyStorage.portableGrid = portableGrid;
        portableGrid.updateStorage();
        Platform.INSTANCE.getMenuOpener().openMenu(player, new PortableGridItemExtendedMenuProvider(
            requireNonNullElse(stack.get(DataComponents.CUSTOM_NAME),
                type == PortableGridType.CREATIVE ? ContentNames.CREATIVE_PORTABLE_GRID : ContentNames.PORTABLE_GRID),
            portableGrid,
            energyStorage,
            diskInventory,
            playerSlotReference
        ));
    }

    private PortableGridEnergyStorage createEnergyStorageInternal(final ItemStack stack,
                                                                  final EnergyItemContext context) {
        if (type == PortableGridType.CREATIVE) {
            return new PortableGridEnergyStorage(CreativeEnergyStorage.INSTANCE);
        }
        return new PortableGridEnergyStorage(createEnergyStorage(stack, context));
    }

    private DiskInventory createDiskInventory(final ItemStack stack,
                                              final DiskInventoryListener listener,
                                              final HolderLookup.Provider registries) {
        final DiskInventory diskInventory = new DiskInventory(listener, 1);
        final TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            final ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, registries,
                blockEntityData.copyTagWithoutId());
            AbstractPortableGridBlockEntity.readDiskInventory(input, diskInventory);
        }
        return diskInventory;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }

    private static class DiskInventoryListener implements Consumer<DiskInventory> {
        private final ItemStack portableGridStack;
        private final HolderLookup.Provider registries;
        @Nullable
        private PortableGrid portableGrid;

        private DiskInventoryListener(final ItemStack portableGridStack, final HolderLookup.Provider registries) {
            this.portableGridStack = portableGridStack;
            this.registries = registries;
        }

        @Override
        public void accept(final DiskInventory inventory) {
            final boolean stillLoading = portableGrid == null;
            if (stillLoading) {
                return;
            }
            setDiskInventory(portableGridStack, inventory, registries);
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
