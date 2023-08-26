package com.refinedmods.refinedstorage2.platform.common.block.entity.wirelesstransmitter;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.WirelessTransmitterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class WirelessTransmitterBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider {
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
    public boolean canPerformOutgoingConnection(final Direction direction) {
        return direction == getDirection();
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction direction, final BlockState other) {
        return colorsAllowConnecting(other) && direction.getOpposite() == getDirection();
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

    public int getRange() {
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
}
