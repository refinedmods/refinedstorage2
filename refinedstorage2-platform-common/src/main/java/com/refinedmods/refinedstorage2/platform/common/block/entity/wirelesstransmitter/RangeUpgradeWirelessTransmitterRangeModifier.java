package com.refinedmods.refinedstorage2.platform.common.block.entity.wirelesstransmitter;

import com.refinedmods.refinedstorage2.platform.api.blockentity.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

public class RangeUpgradeWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        final int amountOfRangeUpgrades = upgradeState.getAmount(Items.INSTANCE.getRangeUpgrade());
        final int rangePerUpgrade = Platform.INSTANCE.getConfig().getUpgrade().getRangeUpgradeRange();
        return range + (amountOfRangeUpgrades * rangePerUpgrade);
    }
}
