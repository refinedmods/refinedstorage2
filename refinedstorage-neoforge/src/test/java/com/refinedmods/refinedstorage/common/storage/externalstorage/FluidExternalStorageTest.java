package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.addFluidToInterface;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.interfaceContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.prepareInterface;
import static com.refinedmods.refinedstorage.common.GameTestUtil.removeFluidFromInterface;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageTestPlots.preparePlot;
import static net.minecraft.world.item.Items.STONE;
import static net.minecraft.world.level.material.Fluids.LAVA;
import static net.minecraft.world.level.material.Fluids.WATER;

public final class FluidExternalStorageTest {
    private FluidExternalStorageTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldExpose(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
            );

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldInsertAllowlist(final GameTestHelper helper) {
        preparePlot(helper, Direction.EAST, (externalStorage, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> insert(helper, network, STONE, 2)));

            // Act
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 14),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
            );

            externalStorage.setPriority(1);
            externalStorage.setFilters(Set.of(asResource(WATER)));
            externalStorage.setFilterMode(FilterMode.ALLOW);

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2)))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount())))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
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
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 16),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 14),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
            );

            externalStorage.setPriority(1);
            externalStorage.setFilters(Set.of(asResource(WATER)));
            externalStorage.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, WATER, Platform.INSTANCE.getBucketAmount() * 2)))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(networkIsAvailable(helper, pos, network ->
                    insert(helper, network, LAVA, Platform.INSTANCE.getBucketAmount())))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 30),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount() * 32),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
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
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
            );

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenExecute(() -> removeFluidFromInterface(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
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
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
            );

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenExecute(() -> removeFluidFromInterface(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount())
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
            prepareInterface(
                helper,
                pos.east(),
                new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
            );

            // Assert
            sequence
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount())
                ))
                .thenExecute(() -> addFluidToInterface(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(interfaceContainsExactly(
                    helper,
                    pos.east(),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(STONE), 2),
                    new ResourceAmount(asResource(WATER), Platform.INSTANCE.getBucketAmount()),
                    new ResourceAmount(asResource(LAVA), Platform.INSTANCE.getBucketAmount() * 2)
                ))
                .thenSucceed();
        });
    }
}
