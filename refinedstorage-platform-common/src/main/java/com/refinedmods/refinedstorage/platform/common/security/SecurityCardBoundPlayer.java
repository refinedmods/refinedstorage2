package com.refinedmods.refinedstorage.platform.common.security;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record SecurityCardBoundPlayer(UUID playerId, String playerName) {
    public static final Codec<SecurityCardBoundPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("playerId").forGetter(SecurityCardBoundPlayer::playerId),
        Codec.STRING.fieldOf("playerName").forGetter(SecurityCardBoundPlayer::playerName)
    ).apply(instance, SecurityCardBoundPlayer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardBoundPlayer> STREAM_CODEC = StreamCodec
        .composite(
            UUIDUtil.STREAM_CODEC, SecurityCardBoundPlayer::playerId,
            ByteBufCodecs.STRING_UTF8, SecurityCardBoundPlayer::playerName,
            SecurityCardBoundPlayer::new
        );

    static SecurityCardBoundPlayer of(final ServerPlayer player) {
        final GameProfile profile = player.getGameProfile();
        return new SecurityCardBoundPlayer(profile.getId(), profile.getName());
    }

    PlayerSecurityActor toSecurityActor() {
        return new PlayerSecurityActor(playerId);
    }
}
