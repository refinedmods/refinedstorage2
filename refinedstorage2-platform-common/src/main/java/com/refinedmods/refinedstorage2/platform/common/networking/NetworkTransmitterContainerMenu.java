package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ValidatedSlot;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class NetworkTransmitterContainerMenu extends AbstractBaseContainerMenu {
    @Nullable
    private final NetworkTransmitterBlockEntity blockEntity;
    private final Player player;
    private final RateLimiter statusUpdateRateLimiter = RateLimiter.create(2);
    private NetworkTransmitterStatus status;

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
                                           final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getNetworkTransmitter(), syncId);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.blockEntity = null;
        this.player = playerInventory.player;
        this.status = new NetworkTransmitterStatus(buf.readBoolean(), buf.readComponent());
        addSlots(playerInventory, new SimpleContainer(1));
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
        final NetworkTransmitterStatus newStatus = blockEntity.getStatus();
        if (newStatus.message().equals(status.message())) {
            return;
        }
        updateStatus(serverPlayer, newStatus);
    }

    private void updateStatus(final ServerPlayer serverPlayer, final NetworkTransmitterStatus newStatus) {
        this.status = newStatus;
        Platform.INSTANCE.getServerToClientCommunications().sendNetworkTransmitterStatus(
            serverPlayer,
            newStatus
        );
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

    NetworkTransmitterStatus getStatus() {
        return status;
    }

    public void setStatus(final NetworkTransmitterStatus status) {
        this.status = status;
    }
}
