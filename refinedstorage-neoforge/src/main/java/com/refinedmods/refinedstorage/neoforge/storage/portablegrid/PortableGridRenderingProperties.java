package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class PortableGridRenderingProperties {
    static final Identifier ACTIVE_MODEL = createIdentifier("block/portable_grid/active");
    static final Identifier INACTIVE_MODEL = createIdentifier("block/portable_grid/inactive");
    static final Identifier INACTIVE_LED_MODEL = createIdentifier("block/disk/led/inactive");
    static final Identifier NEAR_CAPACITY_LED_MODEL = createIdentifier("block/disk/led/near_capacity");
    static final Identifier NORMAL_LED_MODEL = createIdentifier("block/disk/led/normal");
    static final Identifier FULL_LED_MODEL = createIdentifier("block/disk/led/full");

    private PortableGridRenderingProperties() {
    }
}
