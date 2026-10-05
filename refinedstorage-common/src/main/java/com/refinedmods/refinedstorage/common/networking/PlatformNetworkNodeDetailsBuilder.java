package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.NO;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.YES;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class PlatformNetworkNodeDetailsBuilder {
    private static final Component UPGRADES = createTranslation("gui", "network_monitor.details.upgrades");

    private final List<NetworkNodeDetailsElement> elements = new ArrayList<>();

    public PlatformNetworkNodeDetailsBuilder text(final Component text) {
        elements.add(new NetworkNodeDetailsElement.Text(text));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder text(final Component text, final Component rightText) {
        elements.add(new NetworkNodeDetailsElement.Text(text, rightText));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder property(final String name, final Object... args) {
        return text(createDetailsTranslation(name, args));
    }

    public PlatformNetworkNodeDetailsBuilder property(final String name, final boolean value) {
        return text(createDetailsTranslation(name, value ? YES : NO));
    }

    public PlatformNetworkNodeDetailsBuilder resources(final List<Optional<ResourceKey>> resources) {
        elements.add(new NetworkNodeDetailsElement.Resources(resources));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder resourceAmounts(final List<Optional<ResourceAmount>> resources) {
        elements.add(new NetworkNodeDetailsElement.ResourceAmounts(resources));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder progress(final long stored, final long capacity) {
        elements.add(new NetworkNodeDetailsElement.Progress(stored, capacity));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder items(final Component title, final List<ItemStack> items) {
        text(title);
        elements.add(new NetworkNodeDetailsElement.Items(items));
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder filters(@Nullable final FilterMode filterMode,
                                                     final FilterWithFuzzyMode filter) {
        final Component fuzzyMode = createFuzzyModeText(filter.isFuzzyMode());
        if (filterMode != null) {
            text(createDetailsTranslation("filter_mode", createFilterModeText(filterMode)), fuzzyMode);
        } else {
            text(fuzzyMode);
        }
        final List<Optional<ResourceKey>> resources = getResources(filter.getFilterContainer());
        final boolean hasFilters = resources.stream().anyMatch(Optional::isPresent);
        if (filterMode != FilterMode.BLOCK || hasFilters) {
            resources(resources);
        }
        return this;
    }

    public PlatformNetworkNodeDetailsBuilder schedulingMode(final SchedulingModeType schedulingModeType) {
        return property("scheduling_mode", schedulingModeType.getName());
    }

    public PlatformNetworkNodeDetailsBuilder upgrades(final UpgradeContainer upgradeContainer) {
        return items(UPGRADES, upgradeContainer.getItems());
    }

    public List<NetworkNodeDetailsElement> getElements() {
        return elements;
    }

    public NetworkNodeDetails build(final NetworkNodeDetails details) {
        if (!(details instanceof AbstractNetworkNodeDetails baseDetails)) {
            return details;
        }
        return new PlatformNetworkNodeDetails(baseDetails.getEnergyUsage(), baseDetails.isActive(), elements);
    }

    public static MutableComponent createDetailsTranslation(final String name, final Object... args) {
        return createTranslation("gui", "network_monitor.details." + name, args);
    }

    public static Component createFuzzyModeText(final boolean fuzzyMode) {
        return createDetailsTranslation("fuzzy_mode",
            createTranslation("gui", fuzzyMode ? "fuzzy_mode.on" : "fuzzy_mode.off"));
    }

    public static Component createFilterModeText(final FilterMode filterMode) {
        return switch (filterMode) {
            case ALLOW -> createTranslation("gui", "filter_mode.allow");
            case BLOCK -> createTranslation("gui", "filter_mode.block");
        };
    }

    public static List<Optional<ResourceAmount>> getResourceAmounts(final ResourceContainer container) {
        final List<Optional<ResourceAmount>> resources = new ArrayList<>(container.size());
        for (int i = 0; i < container.size(); ++i) {
            resources.add(Optional.ofNullable(container.get(i)));
        }
        return resources;
    }

    public static List<Optional<ResourceKey>> getResources(final ResourceContainer container) {
        final List<Optional<ResourceKey>> resources = new ArrayList<>(container.size());
        for (int i = 0; i < container.size(); ++i) {
            resources.add(Optional.ofNullable(container.getResource(i)));
        }
        return resources;
    }
}
