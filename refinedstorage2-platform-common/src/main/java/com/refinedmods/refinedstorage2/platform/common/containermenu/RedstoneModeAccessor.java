package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.network.node.RedstoneMode;

public interface RedstoneModeAccessor {
    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode redstoneMode);
}
