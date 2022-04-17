package com.refinedmods.refinedstorage2.platform.common.block.entity.storage;

import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InternalNetworkNodeContainerBlockEntity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StorageBlockEntity<T> extends InternalNetworkNodeContainerBlockEntity<StorageNetworkNode<T>> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORAGE_ID = "sid";

    private UUID storageId;

    protected StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, StorageNetworkNode<T> node) {
        super(type, pos, state, node);
    }

    protected abstract PlatformStorage<T> createStorage(Runnable listener);

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        PlatformStorageRepository storageRepository = Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level);
        if (storageId == null) {
            // We are a new block entity, or,
            // we are placed through NBT (#setLevel(Level) -> #load(CompoundTag).
            // When placed through nbt, we need to clean up the storage we create here.
            storageId = UUID.randomUUID();
            getNode().initializeNewStorage(storageRepository, createStorage(storageRepository::markAsChanged), storageId);
        } else {
            // The existing block entity got loaded in the level (#load(CompoundTag) -> #setLevel(Level)).
            getNode().initializeExistingStorage(storageRepository, storageId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_STORAGE_ID)) {
            UUID actualStorageId = tag.getUUID(TAG_STORAGE_ID);
            if (isPlacedThroughNbtPlacement(actualStorageId)) {
                LOGGER.info("Storage {} got placed through nbt, replacing with actual storage {}", storageId, actualStorageId);
                cleanupUnneededInitialStorageAndReinitialize(actualStorageId);
            }
            storageId = actualStorageId;
        }
    }

    private void cleanupUnneededInitialStorageAndReinitialize(UUID actualStorageId) {
        // We got placed through NBT (#setLevel(Level) -> #load(CompoundTag)).
        // Clean up the new storage created in #setLevel(Level).
        PlatformStorageRepository storageRepository = Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level);
        storageRepository.disassemble(storageId).ifPresentOrElse(
                storage -> LOGGER.debug("Unneeded storage {} successfully removed", storageId),
                () -> LOGGER.warn("Unneeded storage {} could not be removed", storageId)
        );
        getNode().initializeExistingStorage(storageRepository, actualStorageId);
    }

    private boolean isPlacedThroughNbtPlacement(UUID otherStorageId) {
        // When placed through nbt, the level is already set and a default new storage will be created.
        return level != null && storageId != null && !storageId.equals(otherStorageId);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (storageId != null) {
            tag.putUUID(TAG_STORAGE_ID, storageId);
        }
    }
}
