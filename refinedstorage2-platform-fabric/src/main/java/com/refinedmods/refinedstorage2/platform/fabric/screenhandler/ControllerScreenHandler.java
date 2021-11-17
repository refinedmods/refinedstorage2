package com.refinedmods.refinedstorage2.platform.fabric.screenhandler;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ControllerScreenHandler extends BaseScreenHandler implements RedstoneModeAccessor {
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;
    private long stored;
    private long capacity;
    private long serverStored;
    private long serverCapacity;
    private ControllerBlockEntity controller;
    private Player playerEntity;

    public ControllerScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getController(), syncId);
        addPlayerInventory(playerInventory, 8, 107);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();

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

    public ControllerScreenHandler(int syncId, Inventory playerInventory, ControllerBlockEntity controller, Player playerEntity) {
        super(Rs2Mod.SCREEN_HANDLERS.getController(), syncId);
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
        if (serverStored != controller.getActualStored() || serverCapacity != controller.getActualCapacity()) {
            serverStored = controller.getActualStored();
            serverCapacity = controller.getActualCapacity();

            ServerPacketUtil.sendToPlayer((ServerPlayer) playerEntity, PacketIds.CONTROLLER_ENERGY, buf -> {
                buf.writeLong(serverStored);
                buf.writeLong(serverCapacity);
            });
        }
    }

    public void setEnergy(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
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
    public void setRedstoneMode(RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }
}
