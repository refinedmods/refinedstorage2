package com.refinedmods.refinedstorage2.fabric.screenhandler;

import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;

public interface RedstoneModeAccessor {
    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode redstoneMode);
}
