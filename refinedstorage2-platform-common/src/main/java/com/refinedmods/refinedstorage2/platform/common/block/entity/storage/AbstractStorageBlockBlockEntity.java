package com.refinedmods.refinedstorage2.platform.common.block.entity.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.StorageConfigurationPersistence;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageConfigurationProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractStorageBlockBlockEntity<T>
    extends AbstractInternalNetworkNodeContainerBlockEntity<StorageNetworkNode<T>>
    implements ExtendedMenuProvider, StorageConfigurationProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORAGE_ID = "sid";

    private final StorageConfigurationPersistence storageConfigurationPersistence;
    private final FilterWithFuzzyMode filter;

    @Nullable
    private UUID storageId;

    protected AbstractStorageBlockBlockEntity(final BlockEntityType<?> type,
                                              final BlockPos pos,
                                              final BlockState state,
                                              final StorageNetworkNode<T> node,
                                              final ResourceType resourceType) {
        super(type, pos, state, node);
        this.filter = new FilterWithFuzzyMode(resourceType, this::setChanged, getNode()::setFilterTemplates, value -> {
        });
        this.storageConfigurationPersistence = new StorageConfigurationPersistence(getNode());
        getNode().setNormalizer(filter.createNormalizer());
    }

    protected abstract PlatformStorage<T> createStorage(Runnable listener);

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        final PlatformStorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        if (storageId == null) {
            // We are a new block entity, or:
            // - We are placed through NBT
            //   (#setLevel(Level) -> #load(CompoundTag)),
            // - We are placed with an existing storage ID
            //   (#setLevel(Level) -> #modifyStorageAfterAlreadyInitialized(UUID)).
            // In both cases listed above we need to clean up the storage we create here.
            storageId = UUID.randomUUID();
            getNode().initializeNewStorage(
                storageRepository,
                createStorage(storageRepository::markAsChanged),
                storageId
            );
        } else {
            // The existing block entity got loaded in the level (#load(CompoundTag) -> #setLevel(Level)).
            getNode().initializeExistingStorage(storageRepository, storageId);
        }
    }

    public void modifyStorageIdAfterAlreadyInitialized(final UUID actualStorageId) {
        LOGGER.info(
            "Storage {} got placed through nbt, replacing with actual storage {}",
            storageId,
            actualStorageId
        );
        cleanupUnneededInitialStorageAndReinitialize(actualStorageId);
        this.storageId = actualStorageId;
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_STORAGE_ID)) {
            final UUID actualStorageId = tag.getUUID(TAG_STORAGE_ID);
            if (isPlacedThroughNbtPlacement(actualStorageId)) {
                LOGGER.info(
                    "Storage {} got placed through nbt, replacing with actual storage {}",
                    storageId,
                    actualStorageId
                );
                cleanupUnneededInitialStorageAndReinitialize(actualStorageId);
            }
            storageId = actualStorageId;
        }

        storageConfigurationPersistence.load(tag);
        filter.load(tag);

        super.load(tag);
    }

    private void cleanupUnneededInitialStorageAndReinitialize(final UUID actualStorageId) {
        // We got placed through NBT (#setLevel(Level) -> #load(CompoundTag)), or,
        // we got placed with an existing storage ID (#setLevel(Level) -> modifyStorageAfterAlreadyInitialized(UUID)).
        // Clean up the storage created earlier in #setLevel(Level).
        final PlatformStorageRepository storageRepository = PlatformApi.INSTANCE
            .getStorageRepository(Objects.requireNonNull(level));
        storageRepository.disassemble(Objects.requireNonNull(storageId)).ifPresentOrElse(
            storage -> LOGGER.debug("Unneeded storage {} successfully removed", storageId),
            () -> LOGGER.warn("Unneeded storage {} could not be removed", storageId)
        );
        getNode().initializeExistingStorage(storageRepository, actualStorageId);
    }

    private boolean isPlacedThroughNbtPlacement(final UUID otherStorageId) {
        // When placed through nbt, the level is already set and a default new storage was created.
        return level != null && storageId != null && !storageId.equals(otherStorageId);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        if (storageId != null) {
            tag.putUUID(TAG_STORAGE_ID, storageId);
        }
        storageConfigurationPersistence.save(tag);
        filter.save(tag);
    }

    @Nullable
    public UUID getStorageId() {
        return storageId;
    }

    @Override
    public AccessMode getAccessMode() {
        return getNode().getAccessMode();
    }

    @Override
    public void setAccessMode(final AccessMode accessMode) {
        getNode().setAccessMode(accessMode);
        setChanged();
    }

    @Override
    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    @Override
    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    protected final ResourceFilterContainer getFilterContainer() {
        return filter.getFilterContainer();
    }

    @Override
    public int getPriority() {
        return getNode().getPriority();
    }

    @Override
    public void setPriority(final int priority) {
        getNode().setPriority(priority);
        setChanged();
    }

    @Override
    public FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    @Override
    public void setFilterMode(final FilterMode mode) {
        getNode().setFilterMode(mode);
        setChanged();
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeLong(getNode().getStored());
        buf.writeLong(getNode().getCapacity());
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }
}
