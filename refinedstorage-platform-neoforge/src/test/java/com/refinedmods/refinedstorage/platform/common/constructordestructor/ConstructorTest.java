package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.assertFluidPresent;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.platform.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.platform.common.constructordestructor.ConstructorTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.FIREWORK_ROCKET;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.WATER;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConstructorTest {
    private ConstructorTest() {
    }

    @GameTest(template = "empty_15x15")
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

    @GameTest(template = "empty_15x15")
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

    @GameTest(template = "empty_15x15")
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
                .thenWaitUntil(() -> helper.assertItemEntityPresent(DIRT, pos.east(), 1))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 9),
                    new ResourceAmount(asResource(STONE), 15)
                ))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
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
