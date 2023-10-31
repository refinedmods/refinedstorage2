package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ControllerContainerMenu extends AbstractBaseContainerMenu {
    private final Player player;
    private final RateLimiter energyUpdateRateLimiter = RateLimiter.create(4);

    private long stored;
    private long capacity;

    @Nullable
    private ControllerBlockEntity controller;

    public ControllerContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getController(), syncId);

        addPlayerInventory(playerInventory, 8, 107);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();
        this.player = playerInventory.player;

        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    public ControllerContainerMenu(final int syncId,
                                   final Inventory playerInventory,
                                   final ControllerBlockEntity controller,
                                   final Player playerEntity) {
        super(Menus.INSTANCE.getController(), syncId);

        this.controller = controller;
        this.stored = controller.getActualStored();
        this.capacity = controller.getActualCapacity();
        this.player = playerEntity;

        addPlayerInventory(playerInventory, 8, 107);

        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            controller::getRedstoneMode,
            controller::setRedstoneMode
        ));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (controller == null) {
            return;
        }
        final boolean changed = stored != controller.getActualStored() || capacity != controller.getActualCapacity();
        if (changed && energyUpdateRateLimiter.tryAcquire()) {
            setEnergyInfo(controller.getActualStored(), controller.getActualCapacity());
            Platform.INSTANCE.getServerToClientCommunications().sendControllerEnergyInfo(
                (ServerPlayer) player,
                stored,
                capacity
            );
        }
    }

    public void setEnergyInfo(final long newStored, final long newCapacity) {
        this.stored = newStored;
        this.capacity = newCapacity;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }
}
