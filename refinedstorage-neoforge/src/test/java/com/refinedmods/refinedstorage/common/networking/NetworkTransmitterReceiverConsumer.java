package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestSequence;

@FunctionalInterface
public interface NetworkTransmitterReceiverConsumer {
    void accept(NetworkTransmitterBlockEntity transmitterBlockEntity,
                BlockPos pos,
                BlockPos receiverPos,
                GameTestSequence gameTestSequence);
}
