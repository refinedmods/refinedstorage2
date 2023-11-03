package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerBlockEntity extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<ControllerNetworkNode>
    implements ExtendedMenuProvider, EnergyBlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerBlockEntity.class);

    private static final String TAG_STORED = "stored";
    private static final String TAG_CAPACITY = "capacity";

    private final ControllerType type;
    private final EnergyStorage energyStorage;
    private final RateLimiter energyStateChangeRateLimiter = RateLimiter.create(1);

    public ControllerBlockEntity(final ControllerType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = createEnergyStorage(type, this);
        this.getNode().setEnergyStorage(energyStorage);
    }

    private static EnergyStorage createEnergyStorage(final ControllerType type, final BlockEntity blockEntity) {
        if (type == ControllerType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            Platform.INSTANCE.getConfig().getController().getEnergyCapacity(),
            blockEntity
        );
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(final ControllerType type) {
        return type == ControllerType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativeController()
            : BlockEntities.INSTANCE.getController();
    }

    public void updateEnergyTypeInLevel(final BlockState state) {
        final ControllerEnergyType currentEnergyType = state.getValue(ControllerBlock.ENERGY_TYPE);
        final ControllerEnergyType newEnergyType = ControllerEnergyType.ofState(getNode().getState());
        if (newEnergyType != currentEnergyType && level != null && energyStateChangeRateLimiter.tryAcquire()) {
            LOGGER.debug(
                "Energy type state change for controller at {}: {} -> {}",
                getBlockPos(),
                currentEnergyType,
                newEnergyType
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(ControllerBlock.ENERGY_TYPE, newEnergyType));
        }
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_STORED, getNode().getActualStored());
        saveRenderingInfo(tag);
    }

    private void saveRenderingInfo(final CompoundTag tag) {
        tag.putLong(TAG_CAPACITY, getNode().getActualCapacity());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_STORED)) {
            energyStorage.receive(tag.getLong(TAG_STORED), Action.EXECUTE);
        }
    }

    @Override
    public Component getDisplayName() {
        return type == ControllerType.CREATIVE ? ContentNames.CREATIVE_CONTROLLER : ContentNames.CONTROLLER;
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

    long getActualStored() {
        return getNode().getActualStored();
    }

    long getActualCapacity() {
        return getNode().getActualCapacity();
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
