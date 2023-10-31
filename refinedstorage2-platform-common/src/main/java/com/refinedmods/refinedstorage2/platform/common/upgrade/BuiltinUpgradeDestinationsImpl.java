package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;

public class BuiltinUpgradeDestinationsImpl implements BuiltinUpgradeDestinations {
    @Override
    public UpgradeDestination getImporter() {
        return UpgradeDestinations.IMPORTER;
    }

    @Override
    public UpgradeDestination getExporter() {
        return UpgradeDestinations.EXPORTER;
    }

    @Override
    public UpgradeDestination getDestructor() {
        return UpgradeDestinations.DESTRUCTOR;
    }

    @Override
    public UpgradeDestination getConstructor() {
        return UpgradeDestinations.CONSTRUCTOR;
    }

    @Override
    public UpgradeDestination getWirelessTransmitter() {
        return UpgradeDestinations.WIRELESS_TRANSMITTER;
    }
}
