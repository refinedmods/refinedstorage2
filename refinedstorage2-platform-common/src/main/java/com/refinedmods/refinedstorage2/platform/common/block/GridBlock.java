package com.refinedmods.refinedstorage2.platform.common.block;

public abstract class GridBlock extends NetworkNodeContainerBlock {
    protected GridBlock(final Properties properties) {
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
