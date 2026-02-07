package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public record RegulatorUpgradeState(double amount, Optional<PlatformResourceKey> resource) {
    public static final Codec<RegulatorUpgradeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("amount").forGetter(RegulatorUpgradeState::amount),
        Codec.optionalField("resource", ResourceCodecs.CODEC, false).forGetter(RegulatorUpgradeState::resource)
    ).apply(instance, RegulatorUpgradeState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RegulatorUpgradeState> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.DOUBLE, RegulatorUpgradeState::amount,
            ByteBufCodecs.optional(ResourceCodecs.STREAM_CODEC), RegulatorUpgradeState::resource,
            RegulatorUpgradeState::new
        );

    static final RegulatorUpgradeState EMPTY = new RegulatorUpgradeState(1, Optional.empty());

    RegulatorUpgradeState withAmount(final double newAmount) {
        return new RegulatorUpgradeState(newAmount, resource);
    }

    RegulatorUpgradeState withResource(@Nullable final PlatformResourceKey newResource) {
        return new RegulatorUpgradeState(amount, Optional.ofNullable(newResource));
    }
}
