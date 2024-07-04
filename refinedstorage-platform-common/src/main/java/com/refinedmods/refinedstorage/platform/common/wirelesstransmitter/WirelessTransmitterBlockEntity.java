package com.refinedmods.refinedstorage.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class WirelessTransmitterBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<WirelessTransmitterData> {
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
    protected InWorldNetworkNodeContainer createMainContainer(final SimpleNetworkNode node) {
        return new WirelessTransmitterNetworkNodeContainer(this, node, MAIN_CONTAINER_NAME, this);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, upgradeContainer.createTag(provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND), provider);
        }
        configureAccordingToUpgrades();
        super.loadAdditional(tag, provider);
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

    @Override
    public WirelessTransmitterData getMenuData() {
        return new WirelessTransmitterData(getRange());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, WirelessTransmitterData> getMenuCodec() {
        return WirelessTransmitterData.STREAM_CODEC;
    }

    int getRange() {
        return PlatformApi.INSTANCE.getWirelessTransmitterRangeModifier().modifyRange(upgradeContainer, 0);
    }

    private void upgradeContainerChanged() {
        setChanged();
        configureAccordingToUpgrades();
    }

    private void configureAccordingToUpgrades() {
        final long baseUsage = Platform.INSTANCE.getConfig().getWirelessTransmitter().getEnergyUsage();
        mainNode.setEnergyUsage(baseUsage + upgradeContainer.getEnergyUsage());
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
