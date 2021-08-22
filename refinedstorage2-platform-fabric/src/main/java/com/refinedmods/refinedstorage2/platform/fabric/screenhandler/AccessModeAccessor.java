package com.refinedmods.refinedstorage2.platform.fabric.screenhandler;

import com.refinedmods.refinedstorage2.api.storage.AccessMode;

public interface AccessModeAccessor {
    AccessMode getAccessMode();

    void setAccessMode(AccessMode accessMode);
}
