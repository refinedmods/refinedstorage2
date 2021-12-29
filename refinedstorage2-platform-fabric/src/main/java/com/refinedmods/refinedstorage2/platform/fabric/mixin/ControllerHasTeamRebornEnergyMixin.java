package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergyAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import team.reborn.energy.api.base.LimitingEnergyStorage;

@Mixin(ControllerBlockEntity.class)
public abstract class ControllerHasTeamRebornEnergyMixin implements ControllerTeamRebornEnergyAccessor {
    @Shadow(remap = false)
    private EnergyStorage energyStorage;

    private LimitingEnergyStorage limitingEnergyStorage;

    @Override
    public LimitingEnergyStorage getLimitingEnergyStorage() {
        if (limitingEnergyStorage == null && energyStorage instanceof ControllerTeamRebornEnergy controllerTeamRebornEnergy) {
            limitingEnergyStorage = new LimitingEnergyStorage(
                    controllerTeamRebornEnergy,
                    controllerTeamRebornEnergy.maxInsert,
                    0
            );
        }
        return limitingEnergyStorage;
    }
}
