package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.EnergyStorage;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.fabric.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screenhandler.ControllerScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyTier;

public class ControllerBlockEntity extends NetworkNodeBlockEntity<ControllerNetworkNode> implements EnergyStorage, ExtendedScreenHandlerFactory, team.reborn.energy.EnergyStorage {
    private final ControllerType type;

    private long lastTypeChanged;
    private ControllerEnergyType lastType = ControllerEnergyType.OFF;

    public ControllerBlockEntity(ControllerType type) {
        super(getBlockEntityType(type));
        this.type = type;
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(ControllerType type) {
        return type == ControllerType.CREATIVE ? Rs2Mod.BLOCK_ENTITIES.getCreativeController() : Rs2Mod.BLOCK_ENTITIES.getController();
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient() && node != null) {
            ControllerEnergyType type = ControllerEnergyType.ofState(node.getState());
            if (type != lastType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > 1000)) {
                this.lastTypeChanged = System.currentTimeMillis();
                this.lastType = type;

                world.setBlockState(pos, world.getBlockState(pos).with(ControllerBlock.ENERGY_TYPE, type));
            }
        }
    }

    @Override
    protected ControllerNetworkNode createNode(World world, BlockPos pos) {
        return new ControllerNetworkNode(
                FabricRs2WorldAdapter.of(world),
                Positions.ofBlockPos(pos),
                FabricNetworkNodeReference.of(world, pos),
                Rs2Config.get().getController().getCapacity(),
                type
        );
    }

    public void receive() {
        if (node != null && type == ControllerType.NORMAL) {
            node.receive(10, Action.EXECUTE);
        }
    }

    @Override
    public long getStored() {
        return node.getStored();
    }

    @Override
    public long getCapacity() {
        return node.getCapacity();
    }

    @Override
    public void setCapacity(long capacity) {
        node.setCapacity(capacity);
    }

    @Override
    public long receive(long amount, Action action) {
        return node.receive(amount, action);
    }

    @Override
    public long extract(long amount, Action action) {
        return node.extract(amount, action);
    }

    @Override
    public Text getDisplayName() {
        return Rs2Mod.createTranslation("block", type == ControllerType.CREATIVE ? "creative_controller" : "controller");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ControllerScreenHandler(syncId, inv, this, player);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeLong(getActualStored());
        buf.writeLong(getActualCapacity());
    }

    public long getActualStored() {
        return node.getActualStored();
    }

    public long getActualCapacity() {
        return node.getActualCapacity();
    }

    @Override
    public double getStored(EnergySide face) {
        return node.getStored();
    }

    @Override
    public void setStored(double amount) {
        long difference = (long) amount - node.getStored();
        if (difference > 0) {
            node.receive(difference, Action.EXECUTE);
        } else {
            node.extract(Math.abs(difference), Action.EXECUTE);
        }
    }

    @Override
    public double getMaxStoredPower() {
        return node.getCapacity();
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INFINITE;
    }
}
