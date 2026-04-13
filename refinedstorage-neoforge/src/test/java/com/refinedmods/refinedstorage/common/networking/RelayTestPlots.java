package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class RelayTestPlots {
    private RelayTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final RelayConsumer consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos relayPos = ZERO.above().above().above();
        helper.setBlock(relayPos, MOD_BLOCKS.getRelay().getDefault().rotated(Direction.UP));

        final BlockPos subnetworkPos = relayPos.above();
        helper.setBlock(subnetworkPos, MOD_BLOCKS.getGrid().getDefault());

        consumer.accept(
            helper.getBlockEntity(relayPos, RelayBlockEntity.class),
            relayPos,
            subnetworkPos,
            helper.startSequence()
        );
    }
}
