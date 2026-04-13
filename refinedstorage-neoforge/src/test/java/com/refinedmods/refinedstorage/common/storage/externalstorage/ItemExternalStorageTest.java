package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import static com.refinedmods.refinedstorage.common.GameTestUtil.addItemToChest;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.containerContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.createStacks;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.prepareChest;
import static com.refinedmods.refinedstorage.common.GameTestUtil.removeItemFromChest;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageTestPlots.preparePlot;
import static net.minecraft.world.item.Items.COBBLESTONE;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;

public final class ItemExternalStorageTest {
    private ItemExternalStorageTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldExpose(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            externalStorage.setPriority(1);
            externalStorage.setFilters(Set.of(asResource(STONE)));
            externalStorage.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, DIRT, 2)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 12),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, false, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            externalStorage.setFuzzyMode(true);
            externalStorage.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            externalStorage.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(DIAMOND_CHESTPLATE), 1)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(damagedDiamondChestplate), 1)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(DIRT), 10, false)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            externalStorage.setPriority(1);
            externalStorage.setFilters(Set.of(asResource(STONE)));
            externalStorage.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, DIRT, 2)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 12),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 12),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, false, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);
            prepareChest(
                helper,
                pos.east(),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            externalStorage.setFuzzyMode(true);
            externalStorage.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            externalStorage.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(DIAMOND_CHESTPLATE), 1, false)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(damagedDiamondChestplate), 1, false)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, asResource(DIRT), 10)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(COBBLESTONE), 3),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtract(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> extract(helper, network, DIRT, 5)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPropagateExternalExtractions(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(2),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenExecute(() -> removeItemFromChest(
                    helper,
                    pos.east(),
                    COBBLESTONE.getDefaultInstance().copyWithCount(3)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPropagatePartialExternalExtractions(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(2),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenExecute(() -> removeItemFromChest(
                    helper,
                    pos.east(),
                    DIRT.getDefaultInstance().copyWithCount(5)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldPropagateExternalInsertions(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(2),
                DIRT.getDefaultInstance().copyWithCount(10)
            );

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenExecute(() -> addItemToChest(
                    helper,
                    pos.east(),
                    COBBLESTONE.getDefaultInstance().copyWithCount(3)
                ))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldNoLongerExposeWhenExternalBlockIsBroken(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(2),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.AIR))
                .thenWaitUntil(() -> helper.assertBlockNotPresent(Blocks.CHEST, pos.east()))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectRedstoneMode(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(2),
                DIRT.getDefaultInstance().copyWithCount(10),
                COBBLESTONE.getDefaultInstance().copyWithCount(3)
            );

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenExecute(() -> externalStorage.setRedstoneMode(RedstoneMode.HIGH))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenExecute(() -> helper.setBlock(pos.west(), Blocks.REDSTONE_BLOCK))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 4),
                    new ResourceAmount(asResource(DIRT), 10),
                    new ResourceAmount(asResource(COBBLESTONE), 3)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldVoidExcess(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                createStacks(STONE, 64, 27)
            );

            externalStorage.setPriority(1);
            externalStorage.setFilters(Set.of(asResource(STONE)));
            externalStorage.setFilterMode(FilterMode.ALLOW);
            externalStorage.setVoidExcess(true);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 64 * 27)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64 * 27)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 10)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 64 * 27)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64 * 27)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectInsertAccessMode(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, false, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(64)
            );

            externalStorage.setAccessMode(AccessMode.INSERT);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 10)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 74)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 74)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> extract(helper, network, STONE, 10, false)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 74)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 74)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectExtractAccessMode(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, false, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            prepareChest(
                helper,
                pos.east(),
                STONE.getDefaultInstance().copyWithCount(64)
            );

            externalStorage.setAccessMode(AccessMode.EXTRACT);

            // Assert
            sequence
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 10, false)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> extract(helper, network, STONE, 10)))
                .thenWaitUntil(containerContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(STONE), 54)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 54)
                ))
                .thenSucceed();
        });
    }
}
