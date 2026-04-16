package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.energy.TransferableBlockEntityEnergy;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<ControllerNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ControllerData>, TransferableBlockEntityEnergy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerBlockEntity.class);

    private static final String TAG_CAPACITY = "capacity";

    private final ControllerType type;
    private final EnergyStorage energyStorage;
    private final RateLimiter energyStateChangeRateLimiter = RateLimiter.create(1);

    public ControllerBlockEntity(final ControllerType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = createEnergyStorage(type, this);
        this.mainNetworkNode.setEnergyStorage(energyStorage);
    }

    private static EnergyStorage createEnergyStorage(final ControllerType type, final BlockEntity blockEntity) {
        if (type == ControllerType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            new EnergyStorageImpl(
                Math.clamp(Platform.INSTANCE.getConfig().getController().getEnergyCapacity(), 1, Long.MAX_VALUE)
            ),
            blockEntity
        );
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(final ControllerType type) {
        return type == ControllerType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativeController()
            : BlockEntities.INSTANCE.getController();
    }

    public void updateEnergyTypeInLevel(final BlockState state) {
        final ControllerEnergyType currentEnergyType = state.getValue(AbstractControllerBlock.ENERGY_TYPE);
        final ControllerEnergyType newEnergyType = ControllerEnergyType.ofState(mainNetworkNode.getState());
        if (newEnergyType != currentEnergyType && level != null && energyStateChangeRateLimiter.tryAcquire()) {
            LOGGER.debug(
                "Energy type state change for controller at {}: {} -> {}",
                getBlockPos(),
                currentEnergyType,
                newEnergyType
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(AbstractControllerBlock.ENERGY_TYPE, newEnergyType));
        }
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        ItemBlockEnergyStorage.store(output, mainNetworkNode.getActualStored());
        saveRenderingInfo(output);
    }

    private void saveRenderingInfo(final ValueOutput output) {
        output.putLong(TAG_CAPACITY, mainNetworkNode.getActualCapacity());
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        ItemBlockEnergyStorage.read(energyStorage, input);
    }

    @Override
    public Component getName() {
        final Component defaultName = type == ControllerType.CREATIVE
            ? ContentNames.CREATIVE_CONTROLLER
            : ContentNames.CONTROLLER;
        return overrideName(defaultName);
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new ControllerContainerMenu(syncId, inv, this, player);
    }

    @Override
    public ControllerData getMenuData() {
        return new ControllerData(getActualStored(), getActualCapacity());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ControllerData> getMenuCodec() {
        return ControllerData.STREAM_CODEC;
    }

    long getActualStored() {
        return mainNetworkNode.getActualStored();
    }

    long getActualCapacity() {
        return mainNetworkNode.getActualCapacity();
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
