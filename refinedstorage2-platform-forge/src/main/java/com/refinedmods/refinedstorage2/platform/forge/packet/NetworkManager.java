package com.refinedmods.refinedstorage2.platform.forge.packet;

import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.ControllerEnergyPacket;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.StorageInfoResponsePacket;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";

    private final ResourceLocation channel = createIdentifier("main_channel");
    private final SimpleChannel handler = NetworkRegistry.ChannelBuilder.named(channel).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

    public NetworkManager() {
        int id = 0;
        handler.registerMessage(id++, ControllerEnergyPacket.class, ControllerEnergyPacket::encode, ControllerEnergyPacket::decode, ControllerEnergyPacket::handle);
        handler.registerMessage(id++, PropertyChangePacket.class, PropertyChangePacket::encode, PropertyChangePacket::decode, PropertyChangePacket::handle);
        handler.registerMessage(id++, StorageInfoRequestPacket.class, StorageInfoRequestPacket::encode, StorageInfoRequestPacket::decode, StorageInfoRequestPacket::handle);
        handler.registerMessage(id++, StorageInfoResponsePacket.class, StorageInfoResponsePacket::encode, StorageInfoResponsePacket::decode, StorageInfoResponsePacket::handle);
    }

    public void send(ServerPlayer player, Object message) {
        handler.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public void send(Object message) {
        handler.sendToServer(message);
    }
}
