package com.refinedmods.refinedstorage2.platform.api.wirelesstransmitter;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
@FunctionalInterface
public interface WirelessTransmitterRangeModifier {
    int modifyRange(UpgradeState upgradeState, int range);

    default int getPriority() {
        return 0;
    }
}
