package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import java.util.Set;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractUpgradeableNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractInternalNetworkNodeContainerBlockEntity<T>
    implements BlockEntityWithDrops {
    private static final String TAG_UPGRADES = "u";

    protected final UpgradeContainer upgradeContainer;
    private RateLimiter rateLimiter = createRateLimiter(0);

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
    public void doWork() {
        if (rateLimiter.tryAcquire()) {
            super.doWork();
        }
    }

    protected void upgradeContainerChanged() {
        configureAccordingToUpgrades();
        setChanged();
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag());
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND));
        }
        configureAccordingToUpgrades();
        super.load(tag);
    }

    private void configureAccordingToUpgrades() {
        final int amountOfSpeedUpgrades = upgradeContainer.countItem(Items.INSTANCE.getSpeedUpgrade());
        final boolean hasStackUpgrade = hasStackUpgrade();
        final long upgradeEnergyUsage = calculateUpgradeEnergyUsage(amountOfSpeedUpgrades, hasStackUpgrade);
        this.rateLimiter = createRateLimiter(amountOfSpeedUpgrades);
        this.setEnergyUsage(upgradeEnergyUsage);
    }

    protected final boolean hasStackUpgrade() {
        return upgradeContainer.hasAnyOf(Set.of(Items.INSTANCE.getStackUpgrade()));
    }

    private long calculateUpgradeEnergyUsage(final long amountOfSpeedUpgrades, final boolean hasStackUpgrade) {
        return (Platform.INSTANCE.getConfig().getUpgrade().getSpeedUpgradeEnergyUsage() * amountOfSpeedUpgrades)
            + (hasStackUpgrade ? Platform.INSTANCE.getConfig().getUpgrade().getStackUpgradeEnergyUsage() : 0L);
    }

    protected abstract void setEnergyUsage(long upgradeEnergyUsage);

    private static RateLimiter createRateLimiter(final int amountOfSpeedUpgrades) {
        return RateLimiter.create((double) amountOfSpeedUpgrades + 1);
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            drops.add(upgradeContainer.getItem(i));
        }
        return drops;
    }
}
