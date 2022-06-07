package com.refinedmods.refinedstorage2.platform.common.block.entity.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageSettingsProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StorageBlockBlockEntity<T> extends InternalNetworkNodeContainerBlockEntity<StorageNetworkNode<T>> implements ExtendedMenuProvider, StorageSettingsProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORAGE_ID = "sid";
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_RESOURCE_FILTER = "rf";
    private static final String TAG_ACCESS_MODE = "am";

    protected final ResourceFilterContainer resourceFilterContainer;

    private UUID storageId;
    private boolean exactMode;

    protected StorageBlockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, StorageNetworkNode<T> node, ResourceType<T> resourceType) {
        super(type, pos, state, node);
        node.setNormalizer(this::normalize);
        this.resourceFilterContainer = new FilteredResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, this::resourceFilterContainerChanged, resourceType);
    }

    private Object normalize(Object value) {
        if (exactMode) {
            return value;
        }
        if (value instanceof FuzzyModeNormalizer<?> fuzzyModeNormalizer) {
            return fuzzyModeNormalizer.normalize();
        }
        return value;
    }

    protected abstract PlatformStorage<T> createStorage(Runnable listener);

    private void resourceFilterContainerChanged() {
        initializeResourceFilter();
        setChanged();
    }

    private void initializeResourceFilter() {
        getNode().setFilterTemplates(resourceFilterContainer.getTemplates());
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        PlatformStorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        if (storageId == null) {
            // We are a new block entity, or:
            // - We are placed through NBT (#setLevel(Level) -> #load(CompoundTag)),
            // - We are placed with an existing storage ID (#setLevel(Level) -> #modifyStorageAfterAlreadyInitialized(UUID)).
            // In both cases listed above we need to clean up the storage we create here.
            storageId = UUID.randomUUID();
            getNode().initializeNewStorage(storageRepository, createStorage(storageRepository::markAsChanged), storageId);
        } else {
            // The existing block entity got loaded in the level (#load(CompoundTag) -> #setLevel(Level)).
            getNode().initializeExistingStorage(storageRepository, storageId);
        }
    }

    public void modifyStorageIdAfterAlreadyInitialized(UUID actualStorageId) {
        LOGGER.info("Storage {} got placed through nbt, replacing with actual storage {}", storageId, actualStorageId);
        cleanupUnneededInitialStorageAndReinitialize(actualStorageId);
        this.storageId = actualStorageId;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains(TAG_STORAGE_ID)) {
            UUID actualStorageId = tag.getUUID(TAG_STORAGE_ID);
            if (isPlacedThroughNbtPlacement(actualStorageId)) {
                LOGGER.info("Storage {} got placed through nbt, replacing with actual storage {}", storageId, actualStorageId);
                cleanupUnneededInitialStorageAndReinitialize(actualStorageId);
            }
            storageId = actualStorageId;
        }

        if (tag.contains(TAG_PRIORITY)) {
            getNode().setPriority(tag.getInt(TAG_PRIORITY));
        }

        if (tag.contains(TAG_FILTER_MODE)) {
            getNode().setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }

        if (tag.contains(TAG_EXACT_MODE)) {
            this.exactMode = tag.getBoolean(TAG_EXACT_MODE);
        }

        if (tag.contains(TAG_ACCESS_MODE)) {
            getNode().setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
        }

        if (tag.contains(TAG_RESOURCE_FILTER)) {
            resourceFilterContainer.load(tag.getCompound(TAG_RESOURCE_FILTER));
        }

        initializeResourceFilter();

        super.load(tag);
    }

    private void cleanupUnneededInitialStorageAndReinitialize(UUID actualStorageId) {
        // We got placed through NBT (#setLevel(Level) -> #load(CompoundTag)), or,
        // we got placed with an existing storage ID (#setLevel(Level) -> modifyStorageAfterAlreadyInitialized(UUID)).
        // Clean up the storage created earlier in #setLevel(Level).
        PlatformStorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        storageRepository.disassemble(storageId).ifPresentOrElse(
                storage -> LOGGER.debug("Unneeded storage {} successfully removed", storageId),
                () -> LOGGER.warn("Unneeded storage {} could not be removed", storageId)
        );
        getNode().initializeExistingStorage(storageRepository, actualStorageId);
    }

    private boolean isPlacedThroughNbtPlacement(UUID otherStorageId) {
        // When placed through nbt, the level is already set and a default new storage was created.
        return level != null && storageId != null && !storageId.equals(otherStorageId);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (storageId != null) {
            tag.putUUID(TAG_STORAGE_ID, storageId);
        }
        tag.put(TAG_RESOURCE_FILTER, resourceFilterContainer.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getNode().getFilterMode()));
        tag.putInt(TAG_PRIORITY, getNode().getPriority());
        tag.putBoolean(TAG_EXACT_MODE, exactMode);
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(getNode().getAccessMode()));
    }

    public UUID getStorageId() {
        return storageId;
    }

    @Override
    public AccessMode getAccessMode() {
        return getNode().getAccessMode();
    }

    @Override
    public void setAccessMode(AccessMode accessMode) {
        getNode().setAccessMode(accessMode);
        setChanged();
    }

    @Override
    public boolean isExactMode() {
        return exactMode;
    }

    @Override
    public void setExactMode(boolean exactMode) {
        this.exactMode = exactMode;
        initializeResourceFilter();
        setChanged();
    }

    @Override
    public int getPriority() {
        return getNode().getPriority();
    }

    @Override
    public void setPriority(int priority) {
        getNode().setPriority(priority);
        setChanged();
    }

    @Override
    public FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    @Override
    public void setFilterMode(FilterMode mode) {
        getNode().setFilterMode(mode);
        setChanged();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeLong(getNode().getStored());
        buf.writeLong(getNode().getCapacity());
        resourceFilterContainer.writeToUpdatePacket(buf);
    }
}
