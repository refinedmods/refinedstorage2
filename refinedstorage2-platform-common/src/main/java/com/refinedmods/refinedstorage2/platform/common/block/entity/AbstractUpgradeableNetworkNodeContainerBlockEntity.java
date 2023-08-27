package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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
    public final void doWork() {
        if (rateLimiter.tryAcquire()) {
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
        LOGGER.info("Reconfiguring {} for upgrades", getBlockPos());
        final int amountOfSpeedUpgrades = upgradeContainer.getAmount(Items.INSTANCE.getSpeedUpgrade());
        this.rateLimiter = createRateLimiter(amountOfSpeedUpgrades);
        this.setEnergyUsage(upgradeContainer.getEnergyUsage());
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
