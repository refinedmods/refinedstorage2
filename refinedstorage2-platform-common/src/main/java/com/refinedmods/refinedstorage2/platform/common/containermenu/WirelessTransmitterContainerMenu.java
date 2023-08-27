package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.wirelesstransmitter.WirelessTransmitterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.UpgradeSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class WirelessTransmitterContainerMenu extends AbstractBaseContainerMenu {
    private final RateLimiter rangeRateLimiter = RateLimiter.create(4);

    private int range;
    @Nullable
    private final WirelessTransmitterBlockEntity wirelessTransmitter;
    private final Player player;

    public WirelessTransmitterContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getWirelessTransmitter(), syncId);
        addSlots(playerInventory, new UpgradeContainer(
            UpgradeDestinations.WIRELESS_TRANSMITTER,
            PlatformApi.INSTANCE.getUpgradeRegistry()
        ));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.range = buf.readInt();
        this.wirelessTransmitter = null;
        this.player = playerInventory.player;
    }

    public WirelessTransmitterContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessTransmitterBlockEntity wirelessTransmitter,
                                            final UpgradeContainer upgradeContainer) {
        super(Menus.INSTANCE.getWirelessTransmitter(), syncId);
        addSlots(playerInventory, upgradeContainer);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            wirelessTransmitter::getRedstoneMode,
            wirelessTransmitter::setRedstoneMode
        ));
        this.range = wirelessTransmitter.getRange();
        this.wirelessTransmitter = wirelessTransmitter;
        this.player = playerInventory.player;
    }

    private void addSlots(final Inventory playerInventory, final UpgradeContainer upgradeContainer) {
        addPlayerInventory(playerInventory, 8, 55);
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            addSlot(new UpgradeSlot(upgradeContainer, i, 187, 6 + (i * 18)));
        }
        transferManager.addBiTransfer(playerInventory, upgradeContainer);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (wirelessTransmitter == null) {
            return;
        }
        final int newRange = wirelessTransmitter.getRange();
        final boolean changed = range != newRange;
        if (changed && rangeRateLimiter.tryAcquire()) {
            this.range = newRange;
            Platform.INSTANCE.getServerToClientCommunications().sendWirelessTransmitterRange(
                (ServerPlayer) player,
                range
            );
        }
    }

    public int getRange() {
        return range;
    }

    public void setRange(final int range) {
        this.range = range;
    }
}
