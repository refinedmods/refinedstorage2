package com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage2.platform.api.wirelesstransmitter.WirelessTransmitter;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeDestinations;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WirelessTransmitterBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider, WirelessTransmitter {
    private static final String TAG_UPGRADES = "u";

    private final UpgradeContainer upgradeContainer = new UpgradeContainer(
        UpgradeDestinations.WIRELESS_TRANSMITTER,
        PlatformApi.INSTANCE.getUpgradeRegistry(),
        this::upgradeContainerChanged
    );

    public WirelessTransmitterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getWirelessTransmitter(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getWirelessTransmitter().getEnergyUsage()
        ));
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag());
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND));
        }
        configureAccordingToUpgrades();
        super.load(tag);
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return;
        }
        sink.tryConnectInSameDimension(worldPosition.relative(myDirection), myDirection.getOpposite());
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        if (!colorsAllowConnecting(connectingState)) {
            return false;
        }
        final Direction myDirection = getDirection();
        return incomingDirection == myDirection;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.WIRELESS_TRANSMITTER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessTransmitterContainerMenu(syncId, inventory, this, upgradeContainer);
    }

    int getRange() {
        return PlatformApi.INSTANCE.getWirelessTransmitterRangeModifier().modifyRange(upgradeContainer, 0);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeInt(getRange());
    }

    private void upgradeContainerChanged() {
        setChanged();
        configureAccordingToUpgrades();
    }

    private void configureAccordingToUpgrades() {
        final long baseUsage = Platform.INSTANCE.getConfig().getWirelessTransmitter().getEnergyUsage();
        getNode().setEnergyUsage(baseUsage + upgradeContainer.getEnergyUsage());
    }

    @Override
    public boolean isInRange(final ResourceKey<Level> dimension, final Vec3 position) {
        final Level level = getLevel();
        if (level == null || level.dimension() != dimension) {
            return false;
        }
        if (!getNode().isActive()) {
            return false;
        }
        final double distance = Math.sqrt(
            Math.pow(getBlockPos().getX() - position.x(), 2)
                + Math.pow(getBlockPos().getY() - position.y(), 2)
                + Math.pow(getBlockPos().getZ() - position.z(), 2)
        );
        return distance <= getRange();
    }
}
