package com.refinedmods.refinedstorage.platform.common.configurationcard;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record ConfigurationCardState(BlockEntityType<?> blockEntityType,
                                     CompoundTag config,
                                     List<Item> upgradeItems) {
    public static final Codec<ConfigurationCardState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("blockEntityType")
            .forGetter(ConfigurationCardState::blockEntityType),
        CompoundTag.CODEC.fieldOf("config")
            .forGetter(ConfigurationCardState::config),
        Codec.list(BuiltInRegistries.ITEM.byNameCodec()).fieldOf("upgradeItems")
            .forGetter(ConfigurationCardState::upgradeItems)
    ).apply(instance, ConfigurationCardState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurationCardState> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE), ConfigurationCardState::blockEntityType,
            ByteBufCodecs.COMPOUND_TAG, ConfigurationCardState::config,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.registry(Registries.ITEM)),
            ConfigurationCardState::upgradeItems,
            ConfigurationCardState::new
        );
}
