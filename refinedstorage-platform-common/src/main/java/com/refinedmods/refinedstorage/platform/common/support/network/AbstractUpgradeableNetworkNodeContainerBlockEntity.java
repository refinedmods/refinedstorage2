package com.refinedmods.refinedstorage.platform.common.support.network;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUpgradeableNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractLevelInteractingNetworkNodeContainerBlockEntity<T>
    implements BlockEntityWithDrops {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AbstractUpgradeableNetworkNodeContainerBlockEntity.class
    );

    private static final String TAG_UPGRADES = "u";

    protected final UpgradeContainer upgradeContainer;
    private int workTickRate = 9;
    private int workTicks;

    protected AbstractUpgradeableNetworkNodeContainerBlockEntity(
        final BlockEntityType<?> type,
        final BlockPos pos,
        final BlockState state,
        final T node,
        final UpgradeDestinations destination
    ) {
        super(type, pos, state, node);
        this.upgradeContainer = new UpgradeContainer(
            destination,
            PlatformApi.INSTANCE.getUpgradeRegistry(),
            this::upgradeContainerChanged
        );
    }

    @Override
    public final void doWork() {
        if (workTicks++ % workTickRate == 0) {
            super.doWork();
            postDoWork();
        }
    }

    protected void postDoWork() {
    }

    private void upgradeContainerChanged() {
        configureAccordingToUpgrades();
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
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
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag(provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND), provider);
        }
        configureAccordingToUpgrades();
        super.loadAdditional(tag, provider);
    }

    private void configureAccordingToUpgrades() {
        LOGGER.debug("Reconfiguring {} for upgrades", getBlockPos());
        final int amountOfSpeedUpgrades = upgradeContainer.getAmount(Items.INSTANCE.getSpeedUpgrade());
        this.workTickRate = 9 - (amountOfSpeedUpgrades * 2);
        this.setEnergyUsage(upgradeContainer.getEnergyUsage());
    }

    protected abstract void setEnergyUsage(long upgradeEnergyUsage);

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            drops.add(upgradeContainer.getItem(i));
        }
        return drops;
    }
}
