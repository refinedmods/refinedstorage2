package com.refinedmods.refinedstorage2.platform.fabric.screenhandler;

import com.refinedmods.refinedstorage2.api.network.node.RedstoneMode;

public interface RedstoneModeAccessor {
    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode redstoneMode);
}
