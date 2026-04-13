package com.refinedmods.refinedstorage.neoforge.storage.diskinterface;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class DiskInterfaceRenderingProperties {
    static final Identifier INACTIVE_BASE_MODEL = createIdentifier("block/disk_interface/inactive");
    static final Identifier INACTIVE_LED_MODEL = createIdentifier("block/disk/led/inactive");
    static final int DISKS = 6;
    static final Vector3f[] TRANSLATIONS = new Vector3f[DISKS];

    static {
        for (int idx = 0; idx < DISKS; ++idx) {
            TRANSLATIONS[idx] = translationAt(idx);
        }
    }

    private DiskInterfaceRenderingProperties() {
    }

    static Identifier getActiveBaseModel(final DyeColor color) {
        return createIdentifier("block/disk_interface/" + color);
    }

    private static Vector3f translationAt(final int idx) {
        final int x = idx < 3 ? 0 : 1;
        final int y = idx % 3;
        return new Vector3f(
            x == 0 ? -(2F / 16F) : -(9F / 16F),
            -((y * 3F) / 16F) - (6F / 16F),
            0
        );
    }
}
