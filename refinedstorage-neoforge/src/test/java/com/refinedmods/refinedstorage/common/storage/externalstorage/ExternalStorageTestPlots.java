package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class ExternalStorageTestPlots {
    private ExternalStorageTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final TriConsumer<
                                AbstractExternalStorageBlockEntity,
                                BlockPos,
                                GameTestSequence> consumer) {
        preparePlot(helper, direction, true, consumer);
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final boolean itemStorage,
                            final TriConsumer<
                                AbstractExternalStorageBlockEntity,
                                BlockPos,
                                GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        if (itemStorage) {
            helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        } else {
            helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getCable().getDefault());
        }
        helper.setBlock(
            ZERO.above().above().north(),
            MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );
        final BlockPos externalStoragePos = ZERO.above().above().above();
        helper.setBlock(externalStoragePos, MOD_BLOCKS.getExternalStorage().getDefault().rotated(direction));
        consumer.accept(
            helper.getBlockEntity(externalStoragePos, AbstractExternalStorageBlockEntity.class),
            externalStoragePos,
            helper.startSequence()
        );
    }
}
