package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.ItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;

final class ConstructorTestPlots {
    private ConstructorTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final TriConsumer<ConstructorBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageType.Variant.ONE_K));
        helper.setBlock(
            ZERO.above().above().north(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageType.Variant.SIXTY_FOUR_B)
        );
        final BlockPos constructorPos = ZERO.above().above().above();
        helper.setBlock(constructorPos, RSBLOCKS.getConstructor().getDefault().rotated(direction));
        consumer.accept(
            requireBlockEntity(helper, constructorPos, ConstructorBlockEntity.class),
            constructorPos,
            helper.startSequence()
        );
    }
}
