package com.refinedmods.refinedstorage.platform.common.controller;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.energy.TransferableBlockEntityEnergy;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerBlockEntity extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<ControllerNetworkNode>
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
        this.mainNode.setEnergyStorage(energyStorage);
    }

    private static EnergyStorage createEnergyStorage(final ControllerType type, final BlockEntity blockEntity) {
        if (type == ControllerType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            new EnergyStorageImpl(Platform.INSTANCE.getConfig().getController().getEnergyCapacity()),
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
        final ControllerEnergyType newEnergyType = ControllerEnergyType.ofState(mainNode.getState());
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
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ItemBlockEnergyStorage.writeToTag(tag, mainNode.getActualStored());
        saveRenderingInfo(tag);
    }

    private void saveRenderingInfo(final CompoundTag tag) {
        tag.putLong(TAG_CAPACITY, mainNode.getActualCapacity());
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ItemBlockEnergyStorage.readFromTag(energyStorage, tag);
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
    public ControllerData getMenuData() {
        return new ControllerData(getActualStored(), getActualCapacity());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ControllerData> getMenuCodec() {
        return ControllerData.STREAM_CODEC;
    }

    long getActualStored() {
        return mainNode.getActualStored();
    }

    long getActualCapacity() {
        return mainNode.getActualCapacity();
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}
