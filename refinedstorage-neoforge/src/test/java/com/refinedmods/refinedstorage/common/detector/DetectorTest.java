package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RedStoneWireBlock;

import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.detector.DetectorTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;

public final class DetectorTest {
    private DetectorTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldNotEmitRedstone(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldEmitRedstoneUnder(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            detector.setConfiguredResource(asResource(DIRT));
            detector.setMode(DetectorMode.UNDER);
            detector.setAmount(5);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(4))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(5))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(6))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(10))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(15))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldUseEqualModeByDefault(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            detector.setConfiguredResource(asResource(DIRT));
            detector.setAmount(9);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(8))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(10))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenExecute(() -> {
                    detector.setConfiguredResource(asResource(damagedDiamondChestplate));
                    detector.setAmount(1);
                })
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(damagedDiamondChestplate), 1)))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldEmitRedstoneEquals(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            detector.setConfiguredResource(asResource(DIRT));
            detector.setMode(DetectorMode.EQUAL);
            detector.setAmount(9);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(8))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(10))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenExecute(() -> {
                    detector.setConfiguredResource(asResource(damagedDiamondChestplate));
                    detector.setAmount(1);
                })
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(damagedDiamondChestplate), 1)))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldEmitRedstoneAbove(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            detector.setConfiguredResource(asResource(DIRT));
            detector.setMode(DetectorMode.ABOVE);
            detector.setAmount(15);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(16))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(14))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(10))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(5))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldEmitRedstoneFuzzy(final GameTestHelper helper) {
        preparePlot(helper, Direction.DOWN, (detector, pos, sequence) -> {
            // Arrange
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
            }));

            // Act
            detector.setFuzzyMode(true);
            detector.setConfiguredResource(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()));
            detector.setMode(DetectorMode.EQUAL);
            detector.setAmount(1);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 0))
                .thenExecute(() -> detector.setAmount(2))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos.north(), RedStoneWireBlock.POWER, 15))
                .thenSucceed();
        });
    }
}
