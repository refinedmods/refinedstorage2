package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class ControllerEnergyLevelItemModelProperty implements RangeSelectItemModelProperty {
    public static final MapCodec<ControllerEnergyLevelItemModelProperty> MAP_CODEC =
        MapCodec.unit(new ControllerEnergyLevelItemModelProperty());
    public static final Identifier NAME = createIdentifier("controller_energy_level");

    @Override
    public float get(final ItemStack stack, @Nullable final ClientLevel clientLevel,
                     @Nullable final ItemOwner itemOwner, final int i) {
        return RefinedStorageApi.INSTANCE.getEnergyStorage(stack, EnergyItemContext.READONLY).map(energyStorage -> {
            if (energyStorage.getStored() == 0) {
                return 1F;
            }
            return (float) energyStorage.getStored() / (float) energyStorage.getCapacity();
        }).orElse(1F);
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}
