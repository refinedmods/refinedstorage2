package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferListener;
import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.AbstractDiskContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;

import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractDiskInterfaceBlockEntity
    extends AbstractDiskContainerBlockEntity<StorageTransferNetworkNode>
    implements StorageTransferListener {
    public static final int AMOUNT_OF_DISKS = 6;

    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_TRANSFER_MODE = "tm";

    private final UpgradeContainer upgradeContainer;

    protected AbstractDiskInterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDiskInterface(), pos, state, new StorageTransferNetworkNode(
            Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsage(),
            Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsagePerDisk(),
            AMOUNT_OF_DISKS
        ));
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.DISK_INTERFACE, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getDiskInterface().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
        }, this::setChanged);
        this.ticker = upgradeContainer.getTicker();
        this.mainNetworkNode.setListener(this);
        this.mainNetworkNode.setTransferQuotaProvider(storage -> {
            if (storage instanceof SerializableStorage serializableStorage) {
                return serializableStorage.getType().getDiskInterfaceTransferQuota(
                    upgradeContainer.has(Items.INSTANCE.getStackUpgrade())
                );
            }
            return 1;
        });
    }

    @Override
    protected void setFilters(final Set<ResourceKey> filters) {
        mainNetworkNode.setFilters(filters);
    }

    @Override
    protected void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        mainNetworkNode.setNormalizer(normalizer);
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_UPGRADES, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(upgradeContainer.getUpgrades()));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        input.read(TAG_UPGRADES, ItemContainerContents.CODEC).ifPresent(upgradeContainer::load);
        super.loadAdditional(input);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        input.getInt(TAG_TRANSFER_MODE).map(TransferModeSettings::getTransferMode).ifPresent(mainNetworkNode::setMode);
        input.getInt(TAG_FILTER_MODE).map(FilterModeSettings::getFilterMode).ifPresent(mainNetworkNode::setFilterMode);
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        output.putInt(TAG_TRANSFER_MODE, TransferModeSettings.getTransferMode(mainNetworkNode.getMode()));
        output.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(mainNetworkNode.getFilterMode()));
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.DISK_INTERFACE);
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
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, upgradeContainer.getItems());
        }
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        setChanged();
    }

    FilterMode getFilterMode() {
        return mainNetworkNode.getFilterMode();
    }

    void setFilterMode(final FilterMode mode) {
        mainNetworkNode.setFilterMode(mode);
        setChanged();
    }

    public StorageTransferMode getTransferMode() {
        return mainNetworkNode.getMode();
    }

    public void setTransferMode(final StorageTransferMode mode) {
        mainNetworkNode.setMode(mode);
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
