package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.diskDriveStorageContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.diskDriveStorageIsEmpty;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveTestPlots.prepareDiskDrive;
import static com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;
import static net.minecraft.world.item.Items.OAK_LOG;
import static net.minecraft.world.item.Items.STONE;

public final class ItemDiskDriveTest {
    private ItemDiskDriveTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldInsert(final GameTestHelper helper) {
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            diskDrive.setFilterMode(FilterMode.ALLOW);
            diskDrive.setFilters(Set.of(asResource(STONE)));

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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            diskDrive.setFuzzyMode(true);
            diskDrive.setFilterMode(FilterMode.ALLOW);
            diskDrive.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));

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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            diskDrive.setFilterMode(FilterMode.BLOCK);
            diskDrive.setFilters(Set.of(asResource(DIRT)));

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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            diskDrive.setFuzzyMode(true);
            diskDrive.setFilterMode(FilterMode.BLOCK);
            diskDrive.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));

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
    public static void shouldRespectInsertPriority(final GameTestHelper helper) {
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            final BlockPos pos2 = pos.west();
            final AbstractDiskDriveBlockEntity diskDrive2 = prepareDiskDrive(helper, true, pos2);

            // Act
            diskDrive.setInsertPriority(2);

            // Assert
            sequence
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenWaitUntil(diskDriveStorageIsEmpty(helper, pos2))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 62);
                    insert(helper, network, DIRT, 12);
                }))
                .thenWaitUntil(diskDriveStorageIsEmpty(helper, pos2))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenExecute(() -> diskDrive2.setInsertPriority(3))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 10);
                }))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(STONE), 10)
                ))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 64),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 74),
                    new ResourceAmount(asResource(DIRT), 12)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtract(final GameTestHelper helper) {
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            diskDrive.setFilterMode(FilterMode.ALLOW);
            diskDrive.setFilters(Set.of(asResource(STONE)));

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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            diskDrive.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenExecute(() -> diskDrive.setFilters(Set.of(asResource(DIRT))))
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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 1024)));

            // Act
            diskDrive.setFilterMode(FilterMode.ALLOW);
            diskDrive.setFilters(Set.of(asResource(STONE)));
            diskDrive.setVoidExcess(true);

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
    public static void shouldRespectExtractPriority(final GameTestHelper helper) {
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            final BlockPos pos2 = pos.east();
            final var diskDrive2 = prepareDiskDrive(helper, true, pos2);

            sequence
                .thenExecute(() -> {
                    diskDrive.setInsertPriority(2);
                    diskDrive2.setInsertPriority(1);
                })
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 10);
                    insert(helper, network, DIRT, 10);
                }))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 10),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(diskDriveStorageIsEmpty(helper, pos2))

                .thenExecute(() -> {
                    diskDrive.setInsertPriority(1);
                    diskDrive2.setInsertPriority(2);
                })
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, STONE, 20);
                    insert(helper, network, DIRT, 4);
                    insert(helper, network, OAK_LOG, 10);
                }))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 10),
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(STONE), 20),
                    new ResourceAmount(asResource(DIRT), 4),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 30),
                    new ResourceAmount(asResource(DIRT), 14),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ));

            // Assert
            sequence
                .thenExecute(() -> {
                    diskDrive.setInsertPriority(0);
                    diskDrive2.setInsertPriority(0);
                    diskDrive.setExtractPriority(2);
                    diskDrive2.setExtractPriority(1);
                })
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, STONE, 15);
                    extract(helper, network, DIRT, 2);
                }))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 8)
                ))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIRT), 4),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIRT), 12),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ))
                .thenExecute(() -> diskDrive2.setExtractPriority(3))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, DIRT, 4);
                }))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 8)
                ))
                .thenWaitUntil(diskDriveStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 15),
                    new ResourceAmount(asResource(DIRT), 8),
                    new ResourceAmount(asResource(OAK_LOG), 10)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectInsertAccessMode(final GameTestHelper helper) {
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Act
            diskDrive.setAccessMode(AccessMode.INSERT);

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
        preparePlot(helper, true, (diskDrive, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 64)));

            // Assert
            sequence
                .thenExecute(() -> diskDrive.setAccessMode(AccessMode.EXTRACT))
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
