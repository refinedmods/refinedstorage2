package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public abstract class AbstractNetworkNodeFactory implements NetworkNodeFactory {
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_ENERGY_USAGE = "energy_usage";

    @Override
    public final NetworkNode create(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final AbstractNetworkNode value = innerCreate(ctx, properties);
        final boolean active = (boolean) properties.getOrDefault(PROPERTY_ACTIVE, true);
        value.setActive(active);
        return value;
    }

    protected final long getEnergyUsage(final Map<String, Object> properties) {
        return (long) properties.getOrDefault(PROPERTY_ENERGY_USAGE, 0L);
    }

    protected abstract AbstractNetworkNode innerCreate(AddNetworkNode ctx, Map<String, Object> properties);
}
