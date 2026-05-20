package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.extract;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageBlockStorageContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageBlockStorageIsEmpty;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockTestPlots.preparePlot;
import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class FluidStorageBlockTest {
    private FluidStorageBlockTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldInsert(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 14);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 16);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertAllowlist(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(WATER)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertBlocklist(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Act
            storageBlock.setFilterMode(FilterMode.BLOCK);
            storageBlock.setFilters(Set.of(asResource(LAVA)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectInsertPriority(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2)));

            final BlockPos pos2 = pos.west();
            helper.setBlock(pos2, MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B));
            final var storageBlock2 = helper.getBlockEntity(pos2, StorageBlockBlockEntity.class);

            // Act
            storageBlock.setInsertPriority(2);

            // Assert
            sequence
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(storageBlockStorageIsEmpty(helper, pos2))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 32);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 12);
                }))
                .thenWaitUntil(storageBlockStorageIsEmpty(helper, pos2))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 34),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 12)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 34),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 12)
                ))
                .thenExecute(() -> storageBlock2.setInsertPriority(3))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 10);
                }))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10)
                ))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 34),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 12)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 44),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 12)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtract(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16);
                insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount());
            }));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 10);
                    extract(helper, network, LAVA, Platform.INSTANCE.getBucketAmount());
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 6)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtractAllowlist(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(WATER)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 8);
                    extract(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldExtractBlocklist(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Act
            storageBlock.setFilterMode(FilterMode.BLOCK);
            storageBlock.setFilters(Set.of(asResource(LAVA)));

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 8);
                    extract(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldVoidExcess(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 64)));

            // Act
            storageBlock.setFilterMode(FilterMode.ALLOW);
            storageBlock.setFilters(Set.of(asResource(WATER)));
            storageBlock.setVoidExcess(true);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 64)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 64)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectExtractPriority(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            final BlockPos pos2 = pos.east();
            helper.setBlock(pos2, MOD_BLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B));
            final var storageBlock2 = helper.getBlockEntity(pos2, StorageBlockBlockEntity.class);

            sequence
                .thenExecute(() -> {
                    storageBlock.setInsertPriority(2);
                    storageBlock2.setInsertPriority(1);
                })
                .thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 10);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 10);
                }))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 10)
                ))
                .thenWaitUntil(storageBlockStorageIsEmpty(helper, pos2))

                .thenExecute(() -> {
                    storageBlock.setInsertPriority(1);
                    storageBlock2.setInsertPriority(2);
                })
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 20);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 4);
                }))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 10),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 10)
                ))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 20),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 4)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 14)
                ));

            // Assert
            sequence
                .thenExecute(() -> {
                    storageBlock.setInsertPriority(0);
                    storageBlock2.setInsertPriority(0);
                    storageBlock.setExtractPriority(2);
                    storageBlock2.setExtractPriority(1);
                })
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 15);
                    extract(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 2);
                }))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 15),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 4)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 15),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 12)
                ))
                .thenExecute(() -> storageBlock2.setExtractPriority(3))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    extract(helper, network, LAVA, Platform.INSTANCE.getBucketAmount() * 4);
                }))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenWaitUntil(storageBlockStorageContainsExactly(
                    helper,
                    pos2,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 15)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 15),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectInsertAccessMode(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Act
            storageBlock.setAccessMode(AccessMode.INSERT);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount());
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(networkIsAvailable(helper, pos, network ->
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16, false)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldRespectExtractAccessMode(final GameTestHelper helper) {
        preparePlot(helper, false, (storageBlock, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16)));

            // Assert
            sequence
                .thenExecute(() -> storageBlock.setAccessMode(AccessMode.EXTRACT))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network -> {
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 16, false);
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount(), false);
                }))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16)
                ))
                .thenExecute(networkIsAvailable(helper, pos, network ->
                    extract(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 8)))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 8)
                ))
                .thenSucceed();
        });
    }
}
