package com.refinedmods.refinedstorage.common.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class ControllerTestPlots {
    private ControllerTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final boolean isCreative,
                            final TriConsumer<ControllerBlockEntity, BlockPos, GameTestSequence> consumer) {
        final BlockPos controllerPos = ZERO.above();
        helper.setBlock(controllerPos, isCreative
            ? MOD_BLOCKS.getCreativeController().getDefault()
            : MOD_BLOCKS.getController().getDefault());
        consumer.accept(
            helper.getBlockEntity(controllerPos, ControllerBlockEntity.class),
            controllerPos,
            helper.startSequence()
        );
    }
}
