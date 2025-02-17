package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeTicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeContainer extends SimpleContainer implements UpgradeState {
    private static final int DEFAULT_WORK_TICK_RATE = 9;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeContainer.class);

    private final UpgradeDestination destination;
    private final UpgradeRegistry registry;
    private final Object2IntMap<UpgradeItem> index = new Object2IntOpenHashMap<>();
    @Nullable
    private final UpgradeContainerListener listener;
    private final int defaultWorkTickRate;
    private final ThrottledNetworkNodeTicker ticker;

    public UpgradeContainer(final UpgradeDestination destination) {
        this(destination, null);
    }

    public UpgradeContainer(final UpgradeDestination destination, @Nullable final UpgradeContainerListener listener) {
        this(destination, listener, DEFAULT_WORK_TICK_RATE);
    }

    public UpgradeContainer(final UpgradeDestination destination,
                            @Nullable final UpgradeContainerListener listener,
                            final int defaultWorkTickRate) {
        super(4);
        this.destination = destination;
        this.registry = RefinedStorageApi.INSTANCE.getUpgradeRegistry();
        this.addListener(container -> updateIndex());
        this.addListener(container -> notifyListener());
        this.listener = listener;
        this.defaultWorkTickRate = defaultWorkTickRate;
        this.ticker = new ThrottledNetworkNodeTicker(defaultWorkTickRate);
    }

    public NetworkNodeTicker getTicker() {
        return ticker;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        final UpgradeMapping upgrade = getAllowedUpgrades().stream()
            .filter(u -> u.upgradeItem() == stack.getItem())
            .findFirst()
            .orElse(null);
        if (upgrade == null) {
            return false;
        }
        final int currentCount = countItem(stack.getItem());
        if (currentCount >= upgrade.maxAmount()) {
            return false;
        }
        return super.canPlaceItem(slot, stack);
    }

    public Set<UpgradeMapping> getAllowedUpgrades() {
        return registry.getByDestination(destination);
    }

    @Override
    public long getRegulatedAmount(final ResourceKey resource) {
        for (int i = 0; i < getContainerSize(); ++i) {
            final ItemStack stack = getItem(i);
            if (stack.getItem() instanceof RegulatorUpgradeItem item) {
                final long regulatedAmount = item.getDesiredAmount(stack, resource);
                if (regulatedAmount > 0) {
                    return regulatedAmount;
                }
            }
        }
        return 0;
    }

    private void updateIndex() {
        LOGGER.debug("Updating upgrade index for {}", destination);
        index.clear();
        for (int i = 0; i < getContainerSize(); ++i) {
            updateIndex(i);
        }
    }

    private void updateIndex(final int slotIndex) {
        final ItemStack stack = getItem(slotIndex);
        if (stack.isEmpty()) {
            return;
        }
        final Item item = stack.getItem();
        if (!(item instanceof UpgradeItem upgradeItem)) {
            return;
        }
        index.put(upgradeItem, index.getInt(upgradeItem) + 1);
    }

    private void notifyListener() {
        if (listener == null) {
            return;
        }
        LOGGER.debug("Reconfiguring for upgrades");
        final int amountOfSpeedUpgrades = getAmount(Items.INSTANCE.getSpeedUpgrade());
        ticker.workTickRate = defaultWorkTickRate - (amountOfSpeedUpgrades * 2);
        listener.updateState(this, getEnergyUsage());
    }

    @Override
    public int getAmount(final UpgradeItem upgradeItem) {
        return index.getInt(upgradeItem);
    }

    public long getEnergyUsage() {
        long usage = 0;
        for (int i = 0; i < getContainerSize(); ++i) {
            final ItemStack stack = getItem(i);
            if (!(stack.getItem() instanceof UpgradeItem upgradeItem)) {
                continue;
            }
            usage += upgradeItem.getEnergyUsage();
        }
        return usage;
    }

    public List<ItemStack> getUpgrades() {
        final List<ItemStack> upgrades = new ArrayList<>();
        for (int i = 0; i < getContainerSize(); ++i) {
            final ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            upgrades.add(stack.copy());
        }
        return upgrades;
    }

    public boolean addUpgrade(final ItemStack upgrade) {
        return addItem(upgrade).isEmpty();
    }

    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < getContainerSize(); ++i) {
            drops.add(getItem(i));
        }
        return drops;
    }

    private static class ThrottledNetworkNodeTicker implements NetworkNodeTicker {
        private int workTickRate;
        private int workTicks;

        private ThrottledNetworkNodeTicker(final int workTickRate) {
            this.workTickRate = workTickRate;
        }

        @Override
        public void tick(final AbstractNetworkNode networkNode) {
            if (workTicks++ % workTickRate == 0) {
                networkNode.doWork();
            }
        }
    }
}
