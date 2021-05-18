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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyTier;

public class ControllerBlockEntity extends NetworkNodeBlockEntity<ControllerNetworkNode> implements EnergyStorage, ExtendedScreenHandlerFactory, team.reborn.energy.EnergyStorage {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORED = "stored";
    private static final int ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final ControllerType type;
    private long lastTypeChanged;

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
            calculateCachedStateIfNecessary();

            ControllerEnergyType type = ControllerEnergyType.ofState(node.getState());
            ControllerEnergyType inWorldType = cachedState.get(ControllerBlock.ENERGY_TYPE);

            if (type != inWorldType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS)) {
                LOGGER.info("Energy type state change for block at {}: {} -> {}", pos, inWorldType, type);

                this.lastTypeChanged = System.currentTimeMillis();

                updateEnergyTypeInWorld(type);
            }
        }
    }

    private void updateEnergyTypeInWorld(ControllerEnergyType type) {
        BlockState newState = world.getBlockState(pos).with(ControllerBlock.ENERGY_TYPE, type);
        updateState(newState);
    }

    @Override
    protected ControllerNetworkNode createNode(World world, BlockPos pos, CompoundTag tag) {
        return new ControllerNetworkNode(
                FabricRs2WorldAdapter.of(world),
                Positions.ofBlockPos(pos),
                FabricNetworkNodeReference.of(world, pos),
                tag.getLong(TAG_STORED),
                Rs2Config.get().getController().getCapacity(),
                type
        );
    }

    public void receive() {
        if (node != null && type == ControllerType.NORMAL) {
            node.receive(50, Action.EXECUTE);
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
    public long receive(long amount, Action action) {
        long remainder = node.receive(amount, action);
        if (remainder != amount && action == Action.EXECUTE) {
            markDirty();
        }
        return remainder;
    }

    @Override
    public long extract(long amount, Action action) {
        long extracted = node.extract(amount, action);
        if (extracted > 0 && action == Action.EXECUTE) {
            markDirty();
        }
        return extracted;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        tag.putLong(TAG_STORED, node.getActualStored());
        return tag;
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
