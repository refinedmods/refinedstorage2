package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;

import java.util.Set;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.STONE;

public final class ItemStorageBlockTest {
    private ItemStorageBlockTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldInsert(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 62);
                    insert(helper, network, DIRT, 12);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertAllowlist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(STONE)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 64);
                    insert(helper, network, DIRT, 12, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 128)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertFuzzyAllowlist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            storageBlock.setFuzzyMode(true);
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(helper, pos))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 12, false);
                    insert(helper, network, DIAMOND_CHESTPLATE, 1);
                    insert(helper, network, asResource(damagedDiamondChestplate), 1);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertBlocklist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            storageBlock.setFilterMode(FilterMode.BLOCK);
            storageBlock.setFilters(Set.of(asResource(DIRT)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 64);
                    insert(helper, network, DIRT, 12, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 128)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            storageBlock.setFuzzyMode(true);
            storageBlock.setFilterMode(FilterMode.BLOCK);
            storageBlock.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(helper, pos))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 12);
                    insert(helper, network, DIAMOND_CHESTPLATE, 1, false);
                    insert(helper, network, asResource(damagedDiamondChestplate), 1, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 12)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtract(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, STONE, 64);
                insert(helper, network, DIRT, 12);
            }));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, STONE, 62);
                    extract(helper, network, DIRT, 12);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtractAllowlist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(STONE)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, STONE, 32);
                    extract(helper, network, DIRT, 12, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 32)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtractBlocklist(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            storageBlock.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenExecute(() -> storageBlock.setFilters(Set.of(asResource(DIRT))))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, STONE, 32);
                    extract(helper, network, DIRT, 12, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 32)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldVoidExcess(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 1024)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(STONE)));
            storageBlock.setVoidExcess(true);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 1024)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 2);
                    insert(helper, network, DIRT, 2, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 1024)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectInsertAccessMode(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            storageBlock.setAccessMode(AccessMode.INSERT);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 64);
                    insert(helper, network, DIRT, 12);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 128),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> extract(helper, network, STONE, 32, false)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 128),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectExtractAccessMode(final GameTestHelper helper) {
        preparePlot(helper, true, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Assert
            sequence
                .thenExecute(() -> storageBlock.setAccessMode(AccessMode.EXTRACT))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 64, false);
                    insert(helper, network, DIRT, 12, false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> extract(helper, network, STONE, 32)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 32)
                ))
                .thenSucceed();
        });
    }
}
