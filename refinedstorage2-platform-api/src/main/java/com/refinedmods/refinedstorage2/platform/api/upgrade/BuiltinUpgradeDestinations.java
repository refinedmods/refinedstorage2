package com.refinedmods.refinedstorage2.platform.api.upgrade;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public interface BuiltinUpgradeDestinations {
    UpgradeDestination getImporter();

    UpgradeDestination getExporter();

    UpgradeDestination getDestructor();

    UpgradeDestination getConstructor();

    UpgradeDestination getWirelessTransmitter();
}
