package com.refinedmods.refinedstorage2.fabric.screenhandler;

import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.fabric.util.ServerPacketUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ControllerScreenHandler extends BaseScreenHandler implements RedstoneModeAccessor {
    private long stored;
    private long capacity;

    private long serverStored;
    private long serverCapacity;
    private ControllerBlockEntity controller;
    private PlayerEntity playerEntity;

    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    public ControllerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
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

        addProperty(redstoneModeProperty);
    }

    public ControllerScreenHandler(int syncId, PlayerInventory playerInventory, ControllerBlockEntity controller, PlayerEntity playerEntity) {
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

        addProperty(redstoneModeProperty);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (serverStored != controller.getActualStored() || serverCapacity != controller.getActualCapacity()) {
            serverStored = controller.getActualStored();
            serverCapacity = controller.getActualCapacity();

            ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerEntity, PacketIds.CONTROLLER_ENERGY, buf -> {
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
