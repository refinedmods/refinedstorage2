package com.refinedmods.refinedstorage.fabric.storage.diskdrive;

import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class DiskDriveRenderingProperties {
    static final Identifier BASE_MODEL = createIdentifier("block/disk_drive/base");
    static final Identifier INACTIVE_LED_MODEL = createIdentifier("block/disk/led/inactive");
    static final int DISKS = 8;
    static final Vector3f[] TRANSLATIONS = new Vector3f[DISKS];

    static {
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 4; ++y) {
                TRANSLATIONS[x + (y * 2)] = translationAt(x, y);
            }
        }
    }

    private DiskDriveRenderingProperties() {
    }

    private static Vector3f translationAt(final int x, final int y) {
        return new Vector3f(
            x == 0 ? -(2F / 16F) : -(9F / 16F),
            -((y * 3F) / 16F) - (2F / 16F),
            0
        );
    }
}
