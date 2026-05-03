package com.refinedmods.refinedstorage.common.controller;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ControllerData(long stored, long capacity, List<ControllerBlockEntity.NodeEnergyEntry> nodeUsages) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ControllerData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, ControllerData::stored,
                    ByteBufCodecs.VAR_LONG, ControllerData::capacity,
                    ByteBufCodecs.collection(
                            ArrayList::new,
                            StreamCodec.composite(
                                    ByteBufCodecs.STRING_UTF8, ControllerBlockEntity.NodeEnergyEntry::name,
                                    ByteBufCodecs.VAR_LONG, ControllerBlockEntity.NodeEnergyEntry::usage,
                                    ByteBufCodecs.VAR_INT, ControllerBlockEntity.NodeEnergyEntry::count,
                                    ItemStack.OPTIONAL_STREAM_CODEC, ControllerBlockEntity.NodeEnergyEntry::icon,
                                    ByteBufCodecs.STRING_UTF8, ControllerBlockEntity.NodeEnergyEntry::translatedName,
                                    ControllerBlockEntity.NodeEnergyEntry::new
                            )
                    ), ControllerData::nodeUsages,
                    ControllerData::new
            );
}
