package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;

final class DetectorModeSettings {
    private static final int UNDER = 0;
    private static final int EQUAL = 1;
    private static final int ABOVE = 2;

    private DetectorModeSettings() {
    }

    public static DetectorMode getDetectorMode(final int detectorMode) {
        return switch (detectorMode) {
            case UNDER -> DetectorMode.UNDER;
            case EQUAL -> DetectorMode.EQUAL;
            case ABOVE -> DetectorMode.ABOVE;
            default -> DetectorMode.EQUAL;
        };
    }

    public static int getDetectorMode(final DetectorMode detectorMode) {
        return switch (detectorMode) {
            case UNDER -> UNDER;
            case EQUAL -> EQUAL;
            case ABOVE -> ABOVE;
        };
    }
}
