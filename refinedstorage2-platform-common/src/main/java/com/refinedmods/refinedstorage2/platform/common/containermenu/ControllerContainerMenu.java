package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ControllerContainerMenu extends AbstractBaseContainerMenu implements RedstoneModeAccessor {
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;
    private long stored;
    private long capacity;
    private long serverStored;
    private long serverCapacity;
    @Nullable
    private ControllerBlockEntity controller;
    private Player playerEntity;
    private final RateLimiter energyUpdateRateLimiter = RateLimiter.create(4);

    public ControllerContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getController(), syncId);
        addPlayerInventory(playerInventory, 8, 107);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();
        this.playerEntity = playerInventory.player;

        this.redstoneModeProperty = TwoWaySyncProperty.forClient(
                0,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneMode.IGNORE,
                redstoneMode -> {
                }
        );

        addDataSlot(redstoneModeProperty);
    }

    public ControllerContainerMenu(final int syncId,
                                   final Inventory playerInventory,
                                   final ControllerBlockEntity controller,
                                   final Player playerEntity) {
        super(Menus.INSTANCE.getController(), syncId);
        this.controller = controller;
        this.serverStored = controller.getActualStored();
        this.serverCapacity = controller.getActualCapacity();
        this.playerEntity = playerEntity;
        addPlayerInventory(playerInventory, 8, 107);

        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
                0,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                controller::getRedstoneMode,
                controller::setRedstoneMode
        );

        addDataSlot(redstoneModeProperty);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (controller == null) {
            return;
        }
        final boolean changed = serverStored != controller.getActualStored()
                || serverCapacity != controller.getActualCapacity();
        if (changed && energyUpdateRateLimiter.tryAcquire()) {
            serverStored = controller.getActualStored();
            serverCapacity = controller.getActualCapacity();
            Platform.INSTANCE.getServerToClientCommunications().sendControllerEnergy(
                    (ServerPlayer) playerEntity,
                    serverStored,
                    serverCapacity
            );
        }
    }

    public void setEnergy(final long newStored, final long newCapacity) {
        this.stored = newStored;
        this.capacity = newCapacity;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeProperty.getDeserialized();
    }

    @Override
    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }
}
