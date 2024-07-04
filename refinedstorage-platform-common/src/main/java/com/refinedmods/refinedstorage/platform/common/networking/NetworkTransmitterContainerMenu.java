package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.S2CPackets;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class NetworkTransmitterContainerMenu extends AbstractBaseContainerMenu {
    @Nullable
    private final NetworkTransmitterBlockEntity blockEntity;
    private final Player player;
    private final RateLimiter statusUpdateRateLimiter = RateLimiter.create(2);
    private NetworkTransmitterData status;

    NetworkTransmitterContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final NetworkTransmitterBlockEntity blockEntity) {
        super(Menus.INSTANCE.getNetworkTransmitter(), syncId);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        this.status = blockEntity.getStatus();
        addSlots(playerInventory, blockEntity.getNetworkCardInventory());
    }

    public NetworkTransmitterContainerMenu(final int syncId,
                                           final Inventory playerInventory,
                                           final NetworkTransmitterData status) {
        super(Menus.INSTANCE.getNetworkTransmitter(), syncId);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.blockEntity = null;
        this.player = playerInventory.player;
        this.status = status;
        addSlots(playerInventory, new NetworkCardInventory());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!statusUpdateRateLimiter.tryAcquire()) {
            return;
        }
        final NetworkTransmitterData newStatus = blockEntity.getStatus();
        if (newStatus.message().equals(status.message())) {
            return;
        }
        updateStatus(serverPlayer, newStatus);
    }

    private void updateStatus(final ServerPlayer serverPlayer, final NetworkTransmitterData newStatus) {
        this.status = newStatus;
        S2CPackets.sendNetworkTransmitterStatus(serverPlayer, newStatus);
    }

    private void addSlots(final Inventory playerInventory, final Container networkCardInventory) {
        addPlayerInventory(playerInventory, 8, 55);
        addSlot(new ValidatedSlot(
            networkCardInventory,
            0,
            8,
            20,
            stack -> stack.getItem() instanceof NetworkCardItem networkCardItem && networkCardItem.isActive(stack)
        ));
        transferManager.addBiTransfer(playerInventory, networkCardInventory);
    }

    NetworkTransmitterData getStatus() {
        return status;
    }

    public void setStatus(final NetworkTransmitterData status) {
        this.status = status;
    }
}
