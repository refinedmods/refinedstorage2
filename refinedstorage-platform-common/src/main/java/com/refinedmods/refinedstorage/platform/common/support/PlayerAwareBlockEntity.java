package com.refinedmods.refinedstorage.platform.common.support;

import java.util.UUID;

public interface PlayerAwareBlockEntity {
    void setPlacedBy(UUID playerId);
}
