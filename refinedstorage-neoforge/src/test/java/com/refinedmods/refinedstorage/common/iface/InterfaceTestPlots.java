package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class InterfaceTestPlots {
    private InterfaceTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final TriConsumer<InterfaceBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos interfacePos = ZERO.above().above().above();
        helper.setBlock(interfacePos, MOD_BLOCKS.getInterface());
        consumer.accept(
            helper.getBlockEntity(interfacePos, InterfaceBlockEntity.class),
            interfacePos,
            helper.startSequence()
        );
    }
}
