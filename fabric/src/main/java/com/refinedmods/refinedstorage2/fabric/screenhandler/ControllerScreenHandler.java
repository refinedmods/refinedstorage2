package com.refinedmods.refinedstorage2.fabric.screenhandler;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.fabric.packet.s2c.ControllerEnergyPacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ControllerScreenHandler extends BaseScreenHandler {
    private long stored;
    private long capacity;

    private long serverStored;
    private long serverCapacity;
    private ControllerBlockEntity controller;
    private PlayerEntity playerEntity;

    public ControllerScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getController(), syncId);
        addPlayerInventory(playerInventory, 8, 107);

        this.stored = buf.readLong();
        this.capacity = buf.readLong();
    }

    public ControllerScreenHandler(int syncId, PlayerInventory playerInventory, ControllerBlockEntity controller, PlayerEntity playerEntity) {
        super(Rs2Mod.SCREEN_HANDLERS.getController(), syncId);
        this.controller = controller;
        this.serverStored = controller.getStored();
        this.serverCapacity = controller.getCapacity();
        this.playerEntity = playerEntity;
        addPlayerInventory(playerInventory, 8, 107);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (serverStored != controller.getStored() || serverCapacity != controller.getCapacity()) {
            serverStored = controller.getStored();
            serverCapacity = controller.getCapacity();

            PacketUtil.sendToPlayer((ServerPlayerEntity) playerEntity, ControllerEnergyPacket.ID, buf -> {
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
}
