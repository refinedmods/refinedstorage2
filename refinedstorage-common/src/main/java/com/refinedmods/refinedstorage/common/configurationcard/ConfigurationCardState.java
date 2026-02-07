package com.refinedmods.refinedstorage.common.configurationcard;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record ConfigurationCardState(BlockEntityType<?> blockEntityType, CompoundTag config, List<ItemStack> upgrades) {
    public static final Codec<ConfigurationCardState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("blockEntityType")
            .forGetter(ConfigurationCardState::blockEntityType),
        CompoundTag.CODEC.fieldOf("config")
            .forGetter(ConfigurationCardState::config),
        Codec.list(ItemStack.CODEC).fieldOf("upgrades")
            .forGetter(ConfigurationCardState::upgrades)
    ).apply(instance, ConfigurationCardState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurationCardState> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE), ConfigurationCardState::blockEntityType,
            ByteBufCodecs.COMPOUND_TAG, ConfigurationCardState::config,
            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC), ConfigurationCardState::upgrades,
            ConfigurationCardState::new
        );
}
