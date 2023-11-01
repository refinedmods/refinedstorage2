package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyType;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

final class DetectorPropertyTypes {
    static final PropertyType<DetectorMode> MODE = new PropertyType<>(
        createIdentifier("detector_mode"),
        DetectorModeSettings::getDetectorMode,
        DetectorModeSettings::getDetectorMode
    );

    private DetectorPropertyTypes() {
    }
}
