package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class NetworkTransmitterReceiverTestPlots {
    private NetworkTransmitterReceiverTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final NetworkTransmitterReceiverConsumer consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos transmitterPos = ZERO.above().above().above();
        helper.setBlock(transmitterPos, MOD_BLOCKS.getNetworkTransmitter().getDefault());

        final BlockPos receiverPos = ZERO.above().above().above().east(10);
        helper.setBlock(receiverPos, MOD_BLOCKS.getNetworkReceiver().getDefault());

        consumer.accept(
            helper.getBlockEntity(transmitterPos, NetworkTransmitterBlockEntity.class),
            transmitterPos,
            receiverPos,
            helper.startSequence()
        );
    }

    public static void checkNetworkTransmitterState(final GameTestHelper helper,
                                                    final BlockPos pos,
                                                    final NetworkTransmitterState expectedState) {
        final var blockEntity = helper.getBlockEntity(
            pos,
            NetworkTransmitterBlockEntity.class
        );
        final NetworkTransmitterState actualState = blockEntity.getBlockState().getValue(NetworkTransmitterBlock.STATE);
        helper.assertTrue(actualState == expectedState, "State of Network Transmitter should be " + expectedState
            + " but is " + actualState);
    }
}
