package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockEntity extends InternalNetworkNodeContainerBlockEntity<ControllerNetworkNode> implements ExtendedMenuProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORED = "stored";
    private static final String TAG_CAPACITY = "capacity";
    private static final int ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final ControllerType type;
    private final EnergyStorage energyStorage;
    private long lastTypeChanged;

    public ControllerBlockEntity(final ControllerType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = Platform.INSTANCE.createEnergyStorage(type, this::setChanged);
        this.getNode().setEnergyStorage(energyStorage);
    }

    public static void serverTick(final BlockState state, final ControllerBlockEntity blockEntity) {
        InternalNetworkNodeContainerBlockEntity.serverTick(state, blockEntity);
        blockEntity.updateEnergyTypeInLevel(state);
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(final ControllerType type) {
        return type == ControllerType.CREATIVE
                ? BlockEntities.INSTANCE.getCreativeController()
                : BlockEntities.INSTANCE.getController();
    }

    public static long getStored(final CompoundTag tag) {
        return tag.contains(TAG_STORED) ? tag.getLong(TAG_STORED) : 0;
    }

    public static long getCapacity(final CompoundTag tag) {
        return tag.contains(TAG_CAPACITY) ? tag.getLong(TAG_CAPACITY) : 0;
    }

    public static boolean hasEnergy(@Nullable final CompoundTag tag) {
        return tag != null && tag.contains(TAG_STORED) && tag.contains(TAG_CAPACITY);
    }

    public void updateEnergyTypeInLevel(final BlockState state) {
        final ControllerEnergyType energyType = ControllerEnergyType.ofState(getNode().getState());
        final ControllerEnergyType inLevelEnergyType = state.getValue(ControllerBlock.ENERGY_TYPE);

        if (energyType != inLevelEnergyType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Energy type state change for block at {}: {} -> {}", getBlockPos(), inLevelEnergyType, energyType);

            this.lastTypeChanged = System.currentTimeMillis();

            updateEnergyTypeInLevel(state, energyType);
        }
    }

    private void updateEnergyTypeInLevel(final BlockState state, final ControllerEnergyType type) {
        if (level != null) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(ControllerBlock.ENERGY_TYPE, type));
        }
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_STORED, getNode().getActualStored());
        // this is not deserialized on purpose and is only here for rendering purposes
        tag.putLong(TAG_CAPACITY, getNode().getActualCapacity());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_STORED)) {
            Platform.INSTANCE.setEnergy(energyStorage, tag.getLong(TAG_STORED));
        }
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", type == ControllerType.CREATIVE ? "creative_controller" : "controller");
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new ControllerContainerMenu(syncId, inv, this, player);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeLong(getActualStored());
        buf.writeLong(getActualCapacity());
    }

    public long getActualStored() {
        return getNode().getActualStored();
    }

    public long getActualCapacity() {
        return getNode().getActualCapacity();
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
