package com.refinedmods.refinedstorage2.platform.common.block.entity;

import java.util.UUID;

public interface PlayerAware {
    void setPlacedBy(UUID playerId);
}
