package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class StorageBlockTestPlots {
    private StorageBlockTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final boolean itemStorage,
                            final TriConsumer<StorageBlockBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        final BlockPos storagePos = ZERO.above().above();
        if (itemStorage) {
            helper.setBlock(storagePos, MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        } else {
            helper.setBlock(storagePos, MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B));
        }
        consumer.accept(
            helper.getBlockEntity(storagePos, StorageBlockBlockEntity.class),
            storagePos,
            helper.startSequence()
        );
    }
}
