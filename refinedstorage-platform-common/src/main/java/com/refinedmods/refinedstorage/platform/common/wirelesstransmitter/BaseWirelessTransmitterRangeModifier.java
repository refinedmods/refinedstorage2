package com.refinedmods.refinedstorage.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.platform.api.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.platform.common.Platform;

public class BaseWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        return Platform.INSTANCE.getConfig().getWirelessTransmitter().getBaseRange() + range;
    }
}
