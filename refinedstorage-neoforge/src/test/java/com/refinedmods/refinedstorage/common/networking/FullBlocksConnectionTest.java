package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.checkBlockEntityActiveness;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.areInSameNetwork;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.checkCableConnection;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.preparePlot;

public final class FullBlocksConnectionTest {
    private FullBlocksConnectionTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldConnectDefaultBlocksToDefaultCable(final GameTestHelper helper) {
        preparePlot(helper, null, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            helper.setBlock(pos.above(), MOD_BLOCKS.getAutocrafter().getDefault()
                .rotated(Direction.UP));
            helper.setBlock(pos.north(), MOD_BLOCKS.getAutocraftingMonitor().getDefault()
                .rotated(OrientedDirection.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCraftingGrid().getDefault()
                .rotated(OrientedDirection.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getDiskInterface().getDefault()
                .rotated(OrientedDirection.SOUTH));
            helper.setBlock(pos.west(), MOD_BLOCKS.getAutocrafterManager().getDefault()
                .rotated(OrientedDirection.WEST));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, true, true, true)))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.north(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.east(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.south(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.west(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), true))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldConnectDefaultBlocksToAnyColoredCable(final GameTestHelper helper) {
        preparePlot(helper, DyeColor.WHITE, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            helper.setBlock(pos.above(), MOD_BLOCKS.getAutocrafter().getDefault()
                .rotated(Direction.UP));
            helper.setBlock(pos.north(), MOD_BLOCKS.getAutocraftingMonitor().getDefault()
                .rotated(OrientedDirection.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCraftingGrid().getDefault()
                .rotated(OrientedDirection.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getDiskInterface().getDefault()
                .rotated(OrientedDirection.SOUTH));
            helper.setBlock(pos.west(), MOD_BLOCKS.getAutocrafterManager().getDefault()
                .rotated(OrientedDirection.WEST));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, true, true, true)))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.north(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.east(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.south(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.west(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), true))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldConnectColoredBlocksToDefaultCable(final GameTestHelper helper) {
        preparePlot(helper, null, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getAutocrafter().get(color)
                .rotated(Direction.UP));
            helper.setBlock(pos.north(), MOD_BLOCKS.getAutocraftingMonitor().get(color)
                .rotated(OrientedDirection.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCraftingGrid().get(color)
                .rotated(OrientedDirection.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getDiskInterface().get(color)
                .rotated(OrientedDirection.SOUTH));
            helper.setBlock(pos.west(), MOD_BLOCKS.getAutocrafterManager().get(color)
                .rotated(OrientedDirection.WEST));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, true, true, true)))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.north(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.east(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.south(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.west(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), true))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldConnectAnyColoredBlocksToAnyColoredCable(final GameTestHelper helper) {
        preparePlot(helper, DyeColor.WHITE, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getAutocrafter().get(color)
                .rotated(Direction.UP));
            helper.setBlock(pos.north(), MOD_BLOCKS.getAutocraftingMonitor().get(color)
                .rotated(OrientedDirection.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCraftingGrid().get(color)
                .rotated(OrientedDirection.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getDiskInterface().get(color)
                .rotated(OrientedDirection.SOUTH));
            helper.setBlock(pos.west(), MOD_BLOCKS.getAutocrafterManager().get(color)
                .rotated(OrientedDirection.WEST));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, true, true, true)))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.north(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.east(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.south(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenWaitUntil(() -> checkBlockEntityActiveness(helper, pos.west(), true))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), true))
                .thenSucceed();
        });
    }
}
