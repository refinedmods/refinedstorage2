package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.energy.TransferableBlockEntityEnergy;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.common.storage.DiskStateChangeListener;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.RedstoneModeSettings;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPortableGridBlockEntity extends BlockEntity
    implements ExtendedMenuProvider<PortableGridData>, ConfigurationCardTarget, TransferableBlockEntityEnergy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPortableGridBlockEntity.class);

    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";
    private static final String TAG_REDSTONE_MODE = "rm";
    private static final String TAG_CUSTOM_NAME = "CustomName";

    @Nullable
    protected Disk disk;
    @Nullable
    private Component customName;

    private final DiskInventory diskInventory;
    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);
    private final EnergyStorage energyStorage;
    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);
    private final PortableGrid grid;
    private final PortableGridType type;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected AbstractPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state);
        this.diskInventory = new DiskInventory(inv -> onDiskChanged(), 1);
        this.energyStorage = createEnergyStorage(type, this);
        this.grid = new InWorldPortableGrid(energyStorage, diskInventory, diskStateListener, this);
        this.type = type;
    }

    static void readDiskInventory(final ValueInput input, final DiskInventory diskInventory) {
        input.read(TAG_DISK_INVENTORY, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(diskInventory.getItems()));
    }

    static void writeDiskInventory(final ValueOutput output, final DiskInventory diskInventory) {
        output.store(TAG_DISK_INVENTORY, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(diskInventory.getItems()));
    }

    static ItemStack getDisk(final CompoundTag tag) {
        return tag.read(TAG_DISK_INVENTORY, ItemContainerContents.CODEC)
            .map(ItemContainerContents::copyOne)
            .orElse(ItemStack.EMPTY);
    }

    private static EnergyStorage createEnergyStorage(final PortableGridType type, final BlockEntity blockEntity) {
        if (type == PortableGridType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            new EnergyStorageImpl(
                Math.clamp(Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity(), 1, Long.MAX_VALUE)
            ),
            blockEntity
        );
    }

    Grid getGrid() {
        return grid;
    }

    void update(final BlockState state) {
        diskStateListener.updateIfNecessary();
        final boolean newActive = grid.isGridActive();
        final boolean activenessNeedsUpdate = state.getValue(PortableGridBlock.ACTIVE) != newActive;
        if (activenessNeedsUpdate && activenessChangeRateLimiter.tryAcquire()) {
            updateActivenessBlockState(state, newActive);
            grid.activeChanged(newActive);
        }
    }

    private void updateActivenessBlockState(final BlockState state, final boolean active) {
        if (level != null) {
            LOGGER.debug(
                "Sending block update at {} due to activeness change: {} -> {}",
                getBlockPos(),
                state.getValue(PortableGridBlock.ACTIVE),
                active
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(PortableGridBlock.ACTIVE, active));
        }
    }

    private void onDiskChanged() {
        grid.updateStorage();
        PlatformUtil.sendBlockUpdateToClient(level, worldPosition);
        setChanged();
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            initialize(level);
        }
    }

    private void initialize(final Level level) {
        diskInventory.setStorageRepository(RefinedStorageApi.INSTANCE.getStorageRepository(level));
        grid.updateStorage();
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        fromClientTag(input);
        readDiskInventory(input, diskInventory);
        ItemBlockEnergyStorage.read(energyStorage, input);
        readConfiguration(input);
        final boolean wasPlacedDismantled = level != null && !level.isClientSide();
        if (wasPlacedDismantled) {
            initialize(level);
        }
        super.loadAdditional(input);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        this.customName = parseCustomNameSafe(input, TAG_CUSTOM_NAME);
        this.redstoneMode = input.getInt(TAG_REDSTONE_MODE)
            .map(RedstoneModeSettings::getRedstoneMode)
            .orElse(RedstoneMode.IGNORE);
    }

    private void fromClientTag(final ValueInput input) {
        final Optional<List<Disk>> optionalDisk = input.read(TAG_DISKS, Disk.LIST_CODEC);
        final boolean potentialDiskDataPresent = optionalDisk.isPresent();
        disk = optionalDisk
            .map(d -> d.toArray(new Disk[0]))
            .map(disks -> disks[0])
            .orElse(null);
        if (potentialDiskDataPresent) {
            onClientDriveStateUpdated();
        }
    }

    protected void onClientDriveStateUpdated() {
        Platform.INSTANCE.requestModelDataUpdateOnClient(this, true);
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        writeDiskInventory(output, diskInventory);
        ItemBlockEnergyStorage.store(output, energyStorage.getStored());
        writeConfiguration(output);
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        if (customName != null) {
            output.store(TAG_CUSTOM_NAME, ComponentSerialization.CODEC, customName);
        }
        output.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(redstoneMode));
    }

    @Override
    protected void applyImplicitComponents(final DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(final DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, customName);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final CompoundTag tag = super.getUpdateTag(registries);
        final List<Disk> diskState = new ArrayList<>();
        final ItemStack diskItem = diskInventory.getItem(0);
        diskState.add(new Disk(diskItem.isEmpty() ? null : diskItem.getItem(), grid.getStorageState()));
        tag.store(TAG_DISKS, Disk.LIST_CODEC, diskState);
        return tag;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        final MutableComponent defaultName = type == PortableGridType.CREATIVE
            ? ContentNames.CREATIVE_PORTABLE_GRID
            : ContentNames.PORTABLE_GRID;
        return customName == null ? defaultName : customName;
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PortableGridBlockContainerMenu(syncId, inventory, this);
    }

    @Override
    public PortableGridData getMenuData() {
        return new PortableGridData(
            GridData.of(grid),
            energyStorage.getStored(),
            energyStorage.getCapacity(),
            Optional.empty()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PortableGridData> getMenuCodec() {
        return PortableGridData.STREAM_CODEC;
    }

    DiskInventory getDiskInventory() {
        return diskInventory;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private static BlockEntityType<AbstractPortableGridBlockEntity> getBlockEntityType(final PortableGridType type) {
        return type == PortableGridType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativePortableGrid()
            : BlockEntities.INSTANCE.getPortableGrid();
    }
}
