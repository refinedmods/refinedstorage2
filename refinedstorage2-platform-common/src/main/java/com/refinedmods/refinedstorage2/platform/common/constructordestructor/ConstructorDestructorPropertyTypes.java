package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

final class ConstructorDestructorPropertyTypes {
    static final PropertyType<Boolean> PICKUP_ITEMS = PropertyTypes.createBooleanProperty(
        createIdentifier("pickup_items")
    );
    static final PropertyType<Boolean> DROP_ITEMS = PropertyTypes.createBooleanProperty(
        createIdentifier("drop_items")
    );

    private ConstructorDestructorPropertyTypes() {
    }
}
