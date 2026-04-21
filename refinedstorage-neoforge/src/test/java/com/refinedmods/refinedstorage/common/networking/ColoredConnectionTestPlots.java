package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.GameTestUtil;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;

import java.util.function.BiConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class ColoredConnectionTestPlots {
    private ColoredConnectionTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            @Nullable final DyeColor firstCableColor,
                            final BiConsumer<BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        final BlockPos defaultCablePos = ZERO.above().above();
        helper.setBlock(defaultCablePos, firstCableColor == null
            ? MOD_BLOCKS.getCable().getDefault()
            : MOD_BLOCKS.getCable().get(firstCableColor));

        consumer.accept(
            defaultCablePos,
            helper.startSequence()
        );
    }

    public static void checkCableConnection(final GameTestHelper helper,
                                            final BlockPos pos,
                                            final CableConnections expectedConnection) {
        final var blockEntity = helper.getBlockEntity(
            pos,
            AbstractCableLikeBlockEntity.class
        );
        final CableConnections actualConnection = blockEntity.getConnections();
        helper.assertTrue(
            actualConnection.equals(expectedConnection),
            "Expected connections [N=" + expectedConnection.north()
                + ", E=" + expectedConnection.east()
                + ", S=" + expectedConnection.south()
                + ", W=" + expectedConnection.west()
                + ", U=" + expectedConnection.up()
                + ", D=" + expectedConnection.down()
                + "] but got [N=" + actualConnection.north()
                + ", E=" + actualConnection.east()
                + ", S=" + actualConnection.south()
                + ", W=" + actualConnection.west()
                + ", U=" + actualConnection.up()
                + ", D=" + actualConnection.down()
                + "]"
        );
    }

    public static void areInSameNetwork(final GameTestHelper helper,
                                        final BlockPos pos1,
                                        final BlockPos pos2,
                                        final boolean shouldSucceed) {
        final Network network1 = GameTestUtil.getNetwork(helper, pos1);
        final Network network2 = GameTestUtil.getNetwork(helper, pos2);
        if (shouldSucceed) {
            helper.assertTrue(network1 == network2, "Expected " + pos1 + " and " + pos2
                + " to be in the same network, but weren't");
        } else {
            helper.assertFalse(network1 == network2, "Expected " + pos1 + " and " + pos2
                + " to not be in the same network, but were");
        }
    }
}
