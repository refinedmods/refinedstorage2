package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.DualEnergyStorage;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.DualEnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.InfiniteDualEnergyStorage;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class ControllerBlockEntity extends InternalNetworkNodeContainerBlockEntity<ControllerNetworkNode> implements ExtendedScreenHandlerFactory {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORED = "stored";
    private static final int ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final ControllerType type;
    private DualEnergyStorage energyStorage;
    private long lastTypeChanged;

    public ControllerBlockEntity(ControllerType type, BlockPos pos, BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = createEnergyStorage(type);
        this.getNode().setEnergyStorage(energyStorage);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ControllerBlockEntity blockEntity) {
        InternalNetworkNodeContainerBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.updateEnergyTypeInLevel(state);
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private DualEnergyStorage createEnergyStorage(ControllerType type) {
        return switch (type) {
            case NORMAL -> createNormalEnergyStorage();
            case CREATIVE -> new InfiniteDualEnergyStorage();
        };
    }

    private DualEnergyStorage createNormalEnergyStorage() {
        return new DualEnergyStorageImpl(Rs2Config.get().getController().getCapacity(), Rs2Config.get().getController().getCapacity()) {
            @Override
            protected void onFinalCommit() {
                setChanged();
            }
        };
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(ControllerType type) {
        return type == ControllerType.CREATIVE ? Rs2Mod.BLOCK_ENTITIES.getCreativeController() : Rs2Mod.BLOCK_ENTITIES.getController();
    }

    public void updateEnergyTypeInLevel(BlockState state) {
        ControllerEnergyType energyType = ControllerEnergyType.ofState(getNode().getState());
        ControllerEnergyType inLevelEnergyType = state.getValue(ControllerBlock.ENERGY_TYPE);

        if (energyType != inLevelEnergyType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Energy type state change for block at {}: {} -> {}", getBlockPos(), inLevelEnergyType, energyType);

            this.lastTypeChanged = System.currentTimeMillis();

            updateEnergyTypeInLevel(state, energyType);
        }
    }

    private void updateEnergyTypeInLevel(BlockState state, ControllerEnergyType type) {
        level.setBlockAndUpdate(getBlockPos(), state.setValue(ControllerBlock.ENERGY_TYPE, type));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_STORED, getNode().getActualStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_STORED)) {
            getNode().receive(tag.getLong(TAG_STORED), Action.EXECUTE);
        }
    }

    @Override
    public Component getDisplayName() {
        return Rs2Mod.createTranslation("block", type == ControllerType.CREATIVE ? "creative_controller" : "controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new ControllerContainerMenu(syncId, inv, this, player);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeLong(getActualStored());
        buf.writeLong(getActualCapacity());
    }

    public long getActualStored() {
        return getNode().getActualStored();
    }

    public long getActualCapacity() {
        return getNode().getActualCapacity();
    }
}
