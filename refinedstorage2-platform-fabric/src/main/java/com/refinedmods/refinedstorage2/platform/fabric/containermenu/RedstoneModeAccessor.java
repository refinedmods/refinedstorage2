package com.refinedmods.refinedstorage2.platform.fabric.containermenu;

import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;

public interface RedstoneModeAccessor {
    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode redstoneMode);
}
