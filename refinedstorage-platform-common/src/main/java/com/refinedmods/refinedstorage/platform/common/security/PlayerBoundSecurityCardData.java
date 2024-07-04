package com.refinedmods.refinedstorage.platform.common.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerBoundSecurityCardData(SecurityCardData securityCardData,
                                          Player boundTo,
                                          List<Player> players) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerBoundSecurityCardData> STREAM_CODEC =
        StreamCodec.composite(
            SecurityCardData.STREAM_CODEC, PlayerBoundSecurityCardData::securityCardData,
            Player.STREAM_CODEC, PlayerBoundSecurityCardData::boundTo,
            ByteBufCodecs.collection(ArrayList::new, Player.STREAM_CODEC), PlayerBoundSecurityCardData::players,
            PlayerBoundSecurityCardData::new
        );

    record Player(UUID id, String name) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Player> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, Player::id,
            ByteBufCodecs.STRING_UTF8, Player::name,
            Player::new
        );

        static Player of(final SecurityCardBoundPlayer securityCardBoundPlayer) {
            return new Player(securityCardBoundPlayer.playerId(), securityCardBoundPlayer.playerName());
        }

        static Player of(final net.minecraft.world.entity.player.Player player) {
            return new Player(player.getUUID(), player.getGameProfile().getName());
        }
    }
}
