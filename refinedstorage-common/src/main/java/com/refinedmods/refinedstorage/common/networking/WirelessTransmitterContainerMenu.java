package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class WirelessTransmitterContainerMenu extends AbstractBaseContainerMenu {
    private final RateLimiter rangeRateLimiter = RateLimiter.create(4);

    @Nullable
    private final WirelessTransmitterBlockEntity wirelessTransmitter;
    private final Player player;

    private int range;
    private boolean active;

    public WirelessTransmitterContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessTransmitterData data) {
        super(Menus.INSTANCE.getWirelessTransmitter(), syncId);
        addSlots(playerInventory, new UpgradeContainer(UpgradeDestinations.WIRELESS_TRANSMITTER));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.range = data.range();
        this.active = data.active();
        this.wirelessTransmitter = null;
        this.player = playerInventory.player;
    }

    WirelessTransmitterContainerMenu(final int syncId,
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
        this.active = wirelessTransmitter.isActive();
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
        final boolean newActive = wirelessTransmitter.isActive();
        final boolean changed = range != newRange || active != newActive;
        if (changed && rangeRateLimiter.tryAcquire()) {
            this.range = newRange;
            this.active = newActive;
            S2CPackets.sendWirelessTransmitterData((ServerPlayer) player, range, active);
        }
    }

    @Override
    public boolean stillValid(final Player p) {
        if (wirelessTransmitter == null) {
            return true;
        }
        return Container.stillValidBlockEntity(wirelessTransmitter, p);
    }

    int getRange() {
        return range;
    }

    boolean isActive() {
        return active;
    }

    public void setRange(final int range) {
        this.range = range;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
