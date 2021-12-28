package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ControllerBlockEntity.class)
public abstract class ControllerHasTeamRebornEnergyMixin {
    @Shadow
    private EnergyStorage energyStorage;

    private ControllerTeamRebornEnergy teamRebornEnergy;

    public ControllerTeamRebornEnergy getTeamRebornEnergy() {
        if (teamRebornEnergy == null) {
            teamRebornEnergy = new ControllerTeamRebornEnergy(energyStorage);
        }
        return teamRebornEnergy;
    }
}
