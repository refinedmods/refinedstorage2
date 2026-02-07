package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_ITEMS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.constructordestructor.DestructorTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND;
import static net.minecraft.world.item.Items.DIAMOND_ORE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class DestructorTest {
    private DestructorTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldBreakBlock(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            helper.setBlock(pos.east(), Blocks.DIRT);

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.DIAMOND_ORE))
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIAMOND_ORE, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIAMOND), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldBreakBlockAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.ALLOW);
            destructor.setFilters(Set.of(asResource(DIRT)));

            helper.setBlock(pos.east(), Blocks.DIRT);

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos.east()))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.STONE))
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.STONE, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldBreakBlockBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.BLOCK);
            destructor.setFilters(Set.of(asResource(STONE)));

            helper.setBlock(pos.east(), Blocks.DIRT);

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos.east()))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.STONE))
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.STONE, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldBreakBlockWithSilkTouchUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            helper.setBlock(pos.east(), Blocks.DIAMOND_ORE);
            destructor.addUpgrade(MOD_ITEMS.getSilkTouchUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.DIAMOND_ORE, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIAMOND_ORE), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPickupItemAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.ALLOW);
            destructor.setPickupItems(true);
            destructor.setFilters(Set.of(asResource(DIRT)));

            helper.spawnItem(DIRT, pos.east());

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertItemEntityNotPresent(DIRT, pos.east(), 1))
                .thenExecute(() -> helper.spawnItem(STONE, pos.east()))
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertItemEntityPresent(STONE, pos.east(), 1))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPickupItemBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            helper.setBlock(pos.east().below(), Blocks.STONE);

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.BLOCK);
            destructor.setPickupItems(true);
            destructor.setFilters(Set.of(asResource(STONE)));

            // Assert
            sequence
                .thenExecute(() -> helper.spawnItem(DIRT, pos.east().getCenter()))
                .thenWaitUntil(() -> helper.assertItemEntityNotPresent(DIRT, pos.east(), 2))
                .thenExecute(() -> helper.spawnItem(STONE, pos.east().getCenter()))
                .thenIdle(20)
                .thenExecute(() -> helper.assertItemEntityPresent(STONE, pos.east(), 2))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 11),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldDrainFluidAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.ALLOW);
            destructor.setFilters(Set.of(asResource(WATER)));

            helper.setBlock(pos.east(), Blocks.WATER);

            // Assert
            sequence
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.WATER, pos.east()))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.DIRT))
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.DIRT, pos.east()))
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
    public static void shouldDrainFluidBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (destructor, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 10);
                insert(helper, network, STONE, 15);
            }));

            // Act
            destructor.setFilterMode(FilterMode.BLOCK);
            destructor.setFilters(Set.of(asResource(LAVA)));

            helper.setBlock(pos.east(), Blocks.WATER);

            // Assert
            sequence
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.WATER, pos.east()))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.LAVA))
                .thenIdle(20)
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.LAVA, pos.east()))
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
}
