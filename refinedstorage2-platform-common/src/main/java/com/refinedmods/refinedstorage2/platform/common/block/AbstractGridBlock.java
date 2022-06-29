package com.refinedmods.refinedstorage2.platform.common.block;

public abstract class AbstractGridBlock extends AbstractNetworkNodeContainerBlock {
    protected AbstractGridBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    protected boolean hasActive() {
        return true;
    }
}
