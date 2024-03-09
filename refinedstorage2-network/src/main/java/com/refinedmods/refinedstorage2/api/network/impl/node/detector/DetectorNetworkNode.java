package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import javax.annotation.Nullable;

public class DetectorNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    private long amount;
    private DetectorMode mode = DetectorMode.EQUAL;
    @Nullable
    private ResourceKey configuredResource;
    @Nullable
    private DetectorAmountStrategy amountStrategy;

    public DetectorNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void setConfiguredResource(@Nullable final ResourceKey configuredResource) {
        this.configuredResource = configuredResource;
    }

    public DetectorMode getMode() {
        return mode;
    }

    public void setMode(final DetectorMode mode) {
        this.mode = mode;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(final long amount) {
        this.amount = amount;
    }

    public void setAmountStrategy(@Nullable final DetectorAmountStrategy amountStrategy) {
        this.amountStrategy = amountStrategy;
    }

    public boolean isActivated() {
        if (configuredResource == null || network == null || !isActive() || amountStrategy == null) {
            return false;
        }
        final long amountInNetwork = amountStrategy.getAmount(network, configuredResource);
        return switch (mode) {
            case UNDER -> amountInNetwork < amount;
            case EQUAL -> amountInNetwork == amount;
            case ABOVE -> amountInNetwork > amount;
        };
    }
}
