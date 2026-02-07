package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_ITEMS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.assertFluidPresent;
import static com.refinedmods.refinedstorage.common.GameTestUtil.assertItemEntityPresentExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.constructordestructor.ConstructorTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.FIREWORK_ROCKET;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class ConstructorTest {
    private ConstructorTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldPlaceBlock(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (constructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            constructor.setFilters(List.of(asResource(DIRT)));

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.DIRT, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 9),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPlaceWater(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (constructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2);
            }));

            // Act
            constructor.setFilters(List.of(asResource(WATER)));

            // Assert
            sequence
                .thenWaitUntil(() -> assertFluidPresent(helper, pos.east(), WATER, FluidState.AMOUNT_FULL))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldDropItem(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (constructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            constructor.setDropItems(true);
            constructor.setFilters(List.of(asResource(DIRT)));

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos.east()))
                .thenWaitUntil(() -> assertItemEntityPresentExactly(
                    helper,
                    DIRT.getDefaultInstance().copyWithCount(1),
                    pos.east(),
                    1
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 9),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldDropItemWithStackUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (constructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 65);
                insert(helper, network, STONE, 15);
            }));

            // Act
            constructor.setDropItems(true);
            constructor.setFilters(List.of(asResource(DIRT)));
            constructor.addUpgrade(MOD_ITEMS.getStackUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenIdle(20)
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos.east()))
                .thenExecute(() -> assertItemEntityPresentExactly(
                    helper,
                    DIRT.getDefaultInstance().copyWithCount(64),
                    pos.east(),
                    1
                ))
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 1),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenIdle(20)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPlaceFireworks(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (constructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, FIREWORK_ROCKET, 15);
            }));

            // Act
            constructor.setFilters(List.of(asResource(FIREWORK_ROCKET)));

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.AIR, pos.east()))
                .thenWaitUntil(() -> helper.assertEntityPresent(EntityType.FIREWORK_ROCKET, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(FIREWORK_ROCKET), 14)
                ))
                .thenSucceed();
        });
    }
}
