package com.refinedmods.refinedstorage2.platform.forge.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.platform.forge.support.render.RotationTranslationModelBaker;

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
