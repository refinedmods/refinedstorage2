package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferListener;
import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.storage.AbstractDiskContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// TODO: upgradeable + level interacting class hierarchy? Disk Interface is copying stuff now... :(
public abstract class AbstractDiskInterfaceBlockEntity
    extends AbstractDiskContainerBlockEntity<StorageTransferNetworkNode>
    implements StorageTransferListener {
    public static final int AMOUNT_OF_DISKS = 6;

    private static final String TAG_UPGRADES = "u";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_TRANSFER_MODE = "tm";

    private final UpgradeContainer upgradeContainer;
    private int workTickRate = 9;
    private int workTicks;

    protected AbstractDiskInterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDiskInterface(), pos, state, new StorageTransferNetworkNode(
            Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsage(),
            Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsagePerDisk(),
            AMOUNT_OF_DISKS
        ));
        this.upgradeContainer = new UpgradeContainer(
            UpgradeDestinations.DISK_INTERFACE,
            PlatformApi.INSTANCE.getUpgradeRegistry(),
            this::upgradeContainerChanged
        );
        this.mainNode.setListener(this);
        this.mainNode.setTransferQuotaProvider(storage -> {
            if (storage instanceof SerializableStorage serializableStorage) {
                return serializableStorage.getType().getDiskInterfaceTransferQuota(
                    upgradeContainer.has(Items.INSTANCE.getStackUpgrade())
                );
            }
            return 1;
        });
    }

    private void upgradeContainerChanged() {
        configureAccordingToUpgrades();
        setChanged();
    }

    private void configureAccordingToUpgrades() {
        final int amountOfSpeedUpgrades = upgradeContainer.getAmount(Items.INSTANCE.getSpeedUpgrade());
        this.workTickRate = 9 - (amountOfSpeedUpgrades * 2);
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsage();
        mainNode.setEnergyUsage(baseEnergyUsage + upgradeContainer.getEnergyUsage());
    }

    @Override
    public final void doWork() {
        if (workTicks++ % workTickRate == 0) {
            super.doWork();
        }
    }

    @Override
    protected void setFilters(final Set<ResourceKey> filters) {
        mainNode.setFilters(filters);
    }

    @Override
    protected void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        mainNode.setNormalizer(normalizer);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND), provider);
        }
        configureAccordingToUpgrades();
        super.loadAdditional(tag, provider);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag(provider));
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(mainNode.getFilterMode()));
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        if (tag.contains(TAG_TRANSFER_MODE)) {
            mainNode.setMode(TransferModeSettings.getTransferMode(tag.getInt(TAG_TRANSFER_MODE)));
        }
        if (tag.contains(TAG_FILTER_MODE)) {
            mainNode.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        tag.putInt(TAG_TRANSFER_MODE, TransferModeSettings.getTransferMode(mainNode.getMode()));
    }

    @Override
    public List<Item> getUpgradeItems() {
        final List<Item> upgradeItems = new ArrayList<>();
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            final ItemStack itemStack = upgradeContainer.getItem(i);
            if (itemStack.isEmpty()) {
                continue;
            }
            upgradeItems.add(itemStack.getItem());
        }
        return upgradeItems;
    }

    @Override
    public boolean addUpgradeItem(final Item upgradeItem) {
        return upgradeContainer.addItem(new ItemStack(upgradeItem)).isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.DISK_INTERFACE;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new DiskInterfaceContainerMenu(
            syncId,
            player,
            this,
            diskInventory,
            filter.getFilterContainer(),
            upgradeContainer
        );
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = super.getDrops();
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            drops.add(upgradeContainer.getItem(i));
        }
        return drops;
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        setChanged();
    }

    FilterMode getFilterMode() {
        return mainNode.getFilterMode();
    }

    void setFilterMode(final FilterMode mode) {
        mainNode.setFilterMode(mode);
        setChanged();
    }

    public StorageTransferMode getTransferMode() {
        return mainNode.getMode();
    }

    public void setTransferMode(final StorageTransferMode mode) {
        mainNode.setMode(mode);
        setChanged();
    }

    @Override
    public void onTransferSuccess(final int index) {
        final ItemStack diskStack = diskInventory.getItem(index);
        if (diskStack.isEmpty()) {
            return;
        }
        for (int newIndex = AMOUNT_OF_DISKS / 2; newIndex < AMOUNT_OF_DISKS; ++newIndex) {
            if (!diskInventory.getItem(newIndex).isEmpty()) {
                continue;
            }
            diskInventory.setItem(index, ItemStack.EMPTY);
            diskInventory.setItem(newIndex, diskStack);
            return;
        }
    }
}
