package com.refinedmods.refinedstorage.platform.common.exporter;

import com.refinedmods.refinedstorage.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.ItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class ExporterTestPlots {
    private ExporterTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final boolean itemTest,
                            final Direction direction,
                            final TriConsumer<ExporterBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageType.Variant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageType.Variant.SIXTY_FOUR_B)
        );
        final BlockPos exporterPos = ZERO.above().above().above();
        helper.setBlock(exporterPos, RSBLOCKS.getExporter().getDefault().rotated(direction));
        helper.setBlock(exporterPos.east(), itemTest ? Blocks.CHEST : Blocks.CAULDRON);
        consumer.accept(
            requireBlockEntity(helper, exporterPos, ExporterBlockEntity.class),
            exporterPos,
            helper.startSequence()
        );
    }
}
