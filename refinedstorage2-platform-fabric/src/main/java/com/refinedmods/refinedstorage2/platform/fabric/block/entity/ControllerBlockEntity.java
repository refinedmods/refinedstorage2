package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerListener;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.ControllerScreenHandler;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyTier;

public class ControllerBlockEntity extends FabricNetworkNodeContainerBlockEntity<ControllerNetworkNode> implements ExtendedScreenHandlerFactory, team.reborn.energy.EnergyStorage, ControllerListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORED = "stored";
    private static final int ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final ControllerType type;
    private long lastTypeChanged;

    public ControllerBlockEntity(ControllerType type, BlockPos pos, BlockState state) {
        super(getBlockEntityType(type), pos, state);
        this.type = type;
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(ControllerType type) {
        return type == ControllerType.CREATIVE ? Rs2Mod.BLOCK_ENTITIES.getCreativeController() : Rs2Mod.BLOCK_ENTITIES.getController();
    }

    public void updateEnergyType(BlockState state) {
        ControllerEnergyType energyType = ControllerEnergyType.ofState(getContainer().getNode().getState());
        ControllerEnergyType inWorldEnergyType = state.get(ControllerBlock.ENERGY_TYPE);

        if (energyType != inWorldEnergyType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Energy type state change for block at {}: {} -> {}", pos, inWorldEnergyType, energyType);

            this.lastTypeChanged = System.currentTimeMillis();

            updateEnergyType(state, energyType);
        }
    }

    private void updateEnergyType(BlockState state, ControllerEnergyType type) {
        world.setBlockState(pos, state.with(ControllerBlock.ENERGY_TYPE, type));
    }

    @Override
    protected ControllerNetworkNode createNode(BlockPos pos, NbtCompound tag) {
        return new ControllerNetworkNode(
                tag != null ? tag.getLong(TAG_STORED) : 0L,
                Rs2Config.get().getController().getCapacity(),
                type,
                this
        );
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag = super.writeNbt(tag);
        tag.putLong(TAG_STORED, getContainer().getNode().getActualStored());
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
        return getContainer().getNode().getActualStored();
    }

    public long getActualCapacity() {
        return getContainer().getNode().getActualCapacity();
    }

    @Override
    public double getStored(EnergySide face) {
        return getContainer().getNode().getStored();
    }

    @Override
    public void setStored(double amount) {
        long difference = (long) amount - getContainer().getNode().getStored();
        if (difference > 0) {
            getContainer().getNode().receive(difference, Action.EXECUTE);
        } else {
            getContainer().getNode().extract(Math.abs(difference), Action.EXECUTE);
        }
    }

    @Override
    public double getMaxStoredPower() {
        return getContainer().getNode().getCapacity();
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INFINITE;
    }

    @Override
    public void onEnergyChanged() {
        markDirty();
    }
}
