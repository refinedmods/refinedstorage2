package com.refinedmods.refinedstorage.platform.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.RotationTranslationModelBaker;

record DiskLedBakers(
    RotationTranslationModelBaker inactiveBaker,
    RotationTranslationModelBaker normalBaker,
    RotationTranslationModelBaker nearCapacityBaker,
    RotationTranslationModelBaker fullBaker
) {
    RotationTranslationModelBaker forState(final StorageState state) {
        return switch (state) {
            case INACTIVE -> inactiveBaker;
            case NEAR_CAPACITY -> nearCapacityBaker;
            case FULL -> fullBaker;
            default -> normalBaker;
        };
    }
}
