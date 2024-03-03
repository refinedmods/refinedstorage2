package com.refinedmods.refinedstorage2.api.network.impl.node.detector;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;

import javax.annotation.Nullable;

public class DetectorNetworkNode extends AbstractNetworkNode {
    private final long energyUsage;

    private long amount;
    private DetectorMode mode = DetectorMode.EQUAL;
    @Nullable
    private ResourceTemplate template;
    @Nullable
    private DetectorAmountStrategy amountStrategy;

    public DetectorNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void setFilterTemplate(@Nullable final ResourceTemplate filterTemplate) {
        this.template = filterTemplate;
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
        if (template == null || network == null || !isActive() || amountStrategy == null) {
            return false;
        }
        final long amountInNetwork = amountStrategy.getAmount(network, template);
        return switch (mode) {
            case UNDER -> amountInNetwork < amount;
            case EQUAL -> amountInNetwork == amount;
            case ABOVE -> amountInNetwork > amount;
        };
    }
}
