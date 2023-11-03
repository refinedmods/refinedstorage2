package com.refinedmods.refinedstorage2.platform.common.support;

import java.util.UUID;

public interface PlayerAwareBlockEntity {
    void setPlacedBy(UUID playerId);
}
