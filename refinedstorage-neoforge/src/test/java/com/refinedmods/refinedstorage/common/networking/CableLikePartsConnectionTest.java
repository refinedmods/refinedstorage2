package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.areInSameNetwork;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.checkCableConnection;
import static com.refinedmods.refinedstorage.common.networking.ColoredConnectionTestPlots.preparePlot;

public final class CableLikePartsConnectionTest {
    private CableLikePartsConnectionTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldConnectAnyColoredCableToDefaultCable(final GameTestHelper helper) {
        preparePlot(helper, null, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.below(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.north(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.south(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.west(), MOD_BLOCKS.getCable().get(color));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, true, true, true)))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.above(),
                    new CableConnections(false, false, false, false, false, true)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.below(),
                    new CableConnections(false, false, false, false, true, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.north(),
                    new CableConnections(false, false, true, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.east(),
                    new CableConnections(false, false, false, true, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.south(),
                    new CableConnections(true, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.west(),
                    new CableConnections(false, true, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), true))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldNotConnectColoredCableToDifferentColoredCable(final GameTestHelper helper) {
        preparePlot(helper, DyeColor.WHITE, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.below(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.north(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.east(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.south(), MOD_BLOCKS.getCable().get(color));
            helper.setBlock(pos.west(), MOD_BLOCKS.getCable().get(color));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.above(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.below(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.north(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.east(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.south(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.west(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.west(), false))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldConnectAnyCableLikePartsToDefaultCable(final GameTestHelper helper) {
        preparePlot(helper, null, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getExporter().get(color).rotated(Direction.UP));
            helper.setBlock(pos.below(), MOD_BLOCKS.getImporter().get(color).rotated(Direction.DOWN));
            helper.setBlock(pos.north(), MOD_BLOCKS.getConstructor().get(color).rotated(Direction.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getDestructor().get(color).rotated(Direction.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getExternalStorage().get(color).rotated(Direction.SOUTH));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(true, true, true, false, true, true)))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.above(),
                    new CableConnections(false, false, false, false, false, true)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.below(),
                    new CableConnections(false, false, false, false, true, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.north(),
                    new CableConnections(false, false, true, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.east(),
                    new CableConnections(false, false, false, true, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), true))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.south(),
                    new CableConnections(true, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), true))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldNotConnectColoredCableLikePartsToDifferentColoredCable(final GameTestHelper helper) {
        preparePlot(helper, DyeColor.WHITE, (pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            final DyeColor color = DyeColor.BLACK;
            helper.setBlock(pos.above(), MOD_BLOCKS.getExporter().get(color).rotated(Direction.UP));
            helper.setBlock(pos.below(), MOD_BLOCKS.getImporter().get(color).rotated(Direction.DOWN));
            helper.setBlock(pos.north(), MOD_BLOCKS.getConstructor().get(color).rotated(Direction.NORTH));
            helper.setBlock(pos.east(), MOD_BLOCKS.getDestructor().get(color).rotated(Direction.EAST));
            helper.setBlock(pos.south(), MOD_BLOCKS.getExternalStorage().get(color).rotated(Direction.SOUTH));

            // Assert
            sequence
                .thenWaitUntil(() -> checkCableConnection(helper, pos,
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.above(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.above(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.below(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.below(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.north(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.north(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.east(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.east(), false))
                .thenWaitUntil(() -> checkCableConnection(helper, pos.south(),
                    new CableConnections(false, false, false, false, false, false)))
                .thenWaitUntil(() -> areInSameNetwork(helper, pos, pos.south(), false))
                .thenSucceed();
        });
    }
}
