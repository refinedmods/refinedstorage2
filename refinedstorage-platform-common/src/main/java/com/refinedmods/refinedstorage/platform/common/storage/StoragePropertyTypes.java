package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public final class StoragePropertyTypes {
    public static final PropertyType<Integer> PRIORITY = PropertyTypes.createIntegerProperty(
        createIdentifier("priority")
    );

    public static final PropertyType<AccessMode> ACCESS_MODE = new PropertyType<>(
        createIdentifier("access_mode"),
        AccessModeSettings::getAccessMode,
        AccessModeSettings::getAccessMode
    );

    public static final PropertyType<Boolean> VOID_EXCESS = PropertyTypes.createBooleanProperty(
        createIdentifier("void_excess")
    );

    private StoragePropertyTypes() {
    }
}
