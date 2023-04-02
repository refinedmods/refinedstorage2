package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Convert to future Fabric FakePlayer API
public final class FakePlayer extends ServerPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FakePlayer.class);
    private static final WeakHashMap<ServerLevel, Map<GameProfile, FakePlayer>> FAKE_PLAYERS = new WeakHashMap<>();
    private static final GameProfile PROFILE = new GameProfile(
        UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"),
        "[Minecraft]"
    );

    private FakePlayer(final ServerLevel level, final GameProfile profile) {
        super(level.getServer(), level, profile);
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
    }

    public static FakePlayer getOrCreate(final ServerLevel level) {
        return getOrCreate(level, PROFILE);
    }

    public static FakePlayer getOrCreate(final ServerLevel level, final GameProfile gameProfile) {
        CoreValidations.validateNotNull(level, "Level cannot be null");
        return FAKE_PLAYERS.computeIfAbsent(level, k -> new HashMap<>())
            .computeIfAbsent(gameProfile, p -> new FakePlayer(level, p));
    }

    public static void release(final ServerLevel serverLevel) {
        final int count = FAKE_PLAYERS.getOrDefault(serverLevel, Collections.emptyMap()).size();
        LOGGER.info("Releasing {} fake players for level {}", count, serverLevel);
        FAKE_PLAYERS.remove(serverLevel);
    }

    @Override
    public void displayClientMessage(final Component chatComponent, final boolean actionBar) {
        // intended, fake player
    }

    @Override
    public void awardStat(final Stat stat, final int amount) {
        // intended, fake player
    }

    @Override
    public boolean isInvulnerableTo(final DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(final Player player) {
        return false;
    }

    @Override
    public void die(final DamageSource source) {
        // intended, fake player
    }

    @Override
    public void tick() {
        // intended, fake player
    }

    @Override
    public void updateOptions(final ServerboundClientInformationPacket packet) {
        // intended, fake player
    }

    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        private FakePlayerNetHandler(final MinecraftServer server, final ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player);
        }

        @Override
        public void tick() {
            // intended, fake player
        }

        @Override
        public void resetPosition() {
            // intended, fake player
        }

        @Override
        public void disconnect(final Component message) {
            // intended, fake player
        }

        @Override
        public void handlePlayerInput(final ServerboundPlayerInputPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleMoveVehicle(final ServerboundMoveVehiclePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleAcceptTeleportPacket(final ServerboundAcceptTeleportationPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleRecipeBookSeenRecipePacket(final ServerboundRecipeBookSeenRecipePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleRecipeBookChangeSettingsPacket(final ServerboundRecipeBookChangeSettingsPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSeenAdvancements(final ServerboundSeenAdvancementsPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleCustomCommandSuggestions(final ServerboundCommandSuggestionPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetCommandBlock(final ServerboundSetCommandBlockPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetCommandMinecart(final ServerboundSetCommandMinecartPacket packet) {
            // intended, fake player
        }

        @Override
        public void handlePickItem(final ServerboundPickItemPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleRenameItem(final ServerboundRenameItemPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetBeaconPacket(final ServerboundSetBeaconPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetStructureBlock(final ServerboundSetStructureBlockPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetJigsawBlock(final ServerboundSetJigsawBlockPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleJigsawGenerate(final ServerboundJigsawGeneratePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSelectTrade(final ServerboundSelectTradePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleEditBook(final ServerboundEditBookPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleEntityTagQuery(final ServerboundEntityTagQuery packet) {
            // intended, fake player
        }

        @Override
        public void handleBlockEntityTagQuery(final ServerboundBlockEntityTagQuery packet) {
            // intended, fake player
        }

        @Override
        public void handleMovePlayer(final ServerboundMovePlayerPacket packet) {
            // intended, fake player
        }

        @Override
        public void teleport(final double x, final double y, final double z, final float yaw, final float pitch) {
            // intended, fake player
        }

        @Override
        public void teleport(final double x, final double y, final double z, final float yaw, final float pitch,
                             final Set<ClientboundPlayerPositionPacket.RelativeArgument> flags) {
            // intended, fake player
        }

        @Override
        public void teleport(final double x, final double y, final double z, final float yaw, final float pitch,
                             final Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeSet,
                             final boolean dismountVehicle) {
            // intended, fake player
        }

        @Override
        public void handlePlayerAction(final ServerboundPlayerActionPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleUseItemOn(final ServerboundUseItemOnPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleUseItem(final ServerboundUseItemPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleTeleportToEntityPacket(final ServerboundTeleportToEntityPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleResourcePackResponse(final ServerboundResourcePackPacket packet) {
            // intended, fake player
        }

        @Override
        public void handlePaddleBoat(final ServerboundPaddleBoatPacket packet) {
            // intended, fake player
        }

        @Override
        public void onDisconnect(final Component message) {
            // intended, fake player
        }

        @Override
        public void send(final Packet<?> packet) {
            // intended, fake player
        }

        @Override
        public void send(final Packet<?> packet, @Nullable final PacketSendListener sendListener) {
            // intended, fake player
        }

        @Override
        public void handleSetCarriedItem(final ServerboundSetCarriedItemPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleChat(final ServerboundChatPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleAnimate(final ServerboundSwingPacket packet) {
            // intended, fake player
        }

        @Override
        public void handlePlayerCommand(final ServerboundPlayerCommandPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleInteract(final ServerboundInteractPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleClientCommand(final ServerboundClientCommandPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleContainerClose(final ServerboundContainerClosePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleContainerClick(final ServerboundContainerClickPacket packet) {
            // intended, fake player
        }

        @Override
        public void handlePlaceRecipe(final ServerboundPlaceRecipePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleContainerButtonClick(final ServerboundContainerButtonClickPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSetCreativeModeSlot(final ServerboundSetCreativeModeSlotPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleSignUpdate(final ServerboundSignUpdatePacket packet) {
            // intended, fake player
        }

        @Override
        public void handleKeepAlive(final ServerboundKeepAlivePacket packet) {
            // intended, fake player
        }

        @Override
        public void handlePlayerAbilities(final ServerboundPlayerAbilitiesPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleClientInformation(final ServerboundClientInformationPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleCustomPayload(final ServerboundCustomPayloadPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleChangeDifficulty(final ServerboundChangeDifficultyPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleLockDifficulty(final ServerboundLockDifficultyPacket packet) {
            // intended, fake player
        }

        @Override
        public void dismount(final double x, final double y, final double z, final float yaw, final float pitch) {
            // intended, fake player
        }

        @Override
        public void ackBlockChangesUpTo(final int sequence) {
            // intended, fake player
        }

        @Override
        public void handleChatCommand(final ServerboundChatCommandPacket packet) {
            // intended, fake player
        }

        @Override
        public void handleChatAck(final ServerboundChatAckPacket packet) {
            // intended, fake player
        }

        @Override
        public void addPendingMessage(final PlayerChatMessage message) {
            // intended, fake player
        }

        @Override
        public void sendPlayerChatMessage(final PlayerChatMessage message, final ChatType.Bound boundChatType) {
            // intended, fake player
        }

        @Override
        public void sendDisguisedChatMessage(final Component content, final ChatType.Bound boundChatType) {
            // intended, fake player
        }

        @Override
        public void handleChatSessionUpdate(final ServerboundChatSessionUpdatePacket packet) {
            // intended, fake player
        }
    }
}
