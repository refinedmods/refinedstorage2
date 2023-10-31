package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeContainer extends SimpleContainer implements UpgradeState {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeContainer.class);

    private final UpgradeDestination destination;
    private final UpgradeRegistry registry;
    private final Object2IntMap<UpgradeItem> index = new Object2IntOpenHashMap<>();

    public UpgradeContainer(final UpgradeDestination destination,
                            final UpgradeRegistry registry) {
        this(destination, registry, () -> {
        });
    }

    public UpgradeContainer(final UpgradeDestination destination,
                            final UpgradeRegistry registry,
                            final Runnable listener) {
        super(4);
        this.destination = destination;
        this.registry = registry;
        this.addListener(container -> updateIndex());
        this.addListener(container -> listener.run());
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

    public <T> OptionalLong getRegulatedAmount(final T resource) {
        return IntStream.range(0, getContainerSize())
            .mapToObj(this::getItem)
            .filter(stack -> stack.getItem() instanceof RegulatorUpgradeItem)
            .flatMapToLong(stack -> ((RegulatorUpgradeItem) stack.getItem()).getDesiredAmount(stack, resource).stream())
            .findFirst();
    }

    @Override
    public void fromTag(final ListTag tag) {
        super.fromTag(tag);
        updateIndex();
    }

    private void updateIndex() {
        LOGGER.info("Updating upgrade index for {}", destination);
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

    @Override
    public boolean has(final UpgradeItem upgradeItem) {
        return index.containsKey(upgradeItem);
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
}
