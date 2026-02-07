package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.MinecraftIntegrationTest;
import com.refinedmods.refinedstorage.common.Platform;

import net.minecraft.gametest.framework.GameTestHelper;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.energyStoredExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.controller.ControllerTestPlots.preparePlot;

public final class ControllerTest {
    private ControllerTest() {
    }

    @MinecraftIntegrationTest
    public static void shouldConsumeEnergy(final GameTestHelper helper) {
        preparePlot(helper, false, (controller, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final EnergyStorage energyStorage = controller.getEnergyStorage();
            energyStorage.receive(energyStorage.getCapacity(), Action.EXECUTE);

            // Assert
            sequence
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity(), helper))
                .thenWaitUntil(() -> helper.setBlock(pos.above(), MOD_BLOCKS.getGrid().getDefault()))
                .thenIdle(1)
                .thenExecute(() -> energyStoredExactly(
                    energyStorage.getStored(),
                    energyStorage.getCapacity() - Platform.INSTANCE.getConfig().getGrid().getEnergyUsage(),
                    helper
                ))
                .thenIdle(9)
                .thenExecute(() -> energyStoredExactly(
                    energyStorage.getStored(),
                    energyStorage.getCapacity() - Platform.INSTANCE.getConfig().getGrid().getEnergyUsage() * 10,
                    helper
                ))
                .thenSucceed();
        });
    }

    @MinecraftIntegrationTest
    public static void shouldNotConsumeEnergy(final GameTestHelper helper) {
        preparePlot(helper, true, (controller, pos, sequence) -> {
            // Arrange
            final EnergyStorage energyStorage = controller.getEnergyStorage();

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Assert
            sequence
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity(), helper))
                .thenWaitUntil(() -> helper.setBlock(pos.above(), MOD_BLOCKS.getGrid().getDefault()))
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity(), helper))
                .thenSucceed();
        });
    }
}
