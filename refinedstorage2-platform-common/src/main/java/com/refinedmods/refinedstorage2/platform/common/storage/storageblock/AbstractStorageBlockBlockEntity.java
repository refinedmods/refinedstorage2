package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.NetworkNodeMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractStorageBlockBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<StorageNetworkNode>
    implements NetworkNodeMenuProvider, StorageBlockEntity, StorageNetworkNode.Provider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageBlockBlockEntity.class);

    private static final String TAG_STORAGE_ID = "sid";

    protected final StorageConfigurationContainerImpl configContainer;
    private final FilterWithFuzzyMode filter;

    @Nullable
    private UUID storageId;

    protected AbstractStorageBlockBlockEntity(final BlockEntityType<?> type,
                                              final BlockPos pos,
                                              final BlockState state,
                                              final StorageNetworkNode node,
                                              final ResourceFactory resourceFactory) {
        super(type, pos, state, node);
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(resourceFactory),
            this::setChanged,
            mainNode::setFilters
        );
        this.configContainer = new StorageConfigurationContainerImpl(
            mainNode,
            filter,
            this::setChanged,
            this::getRedstoneMode,
            this::setRedstoneMode
        );
        mainNode.setNormalizer(filter.createNormalizer());
    }

    protected abstract Storage createStorage(Runnable listener);

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        if (storageId == null) {
            // We are a new block entity, or:
            // - We are placed through NBT
            //   (#setLevel(Level) -> #load(CompoundTag)),
            // - We are placed with an existing storage ID
            //   (#setLevel(Level) -> #modifyStorageAfterAlreadyInitialized(UUID)).
            // In both cases listed above we need to clean up the storage we create here.
            storageId = UUID.randomUUID();
            final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
            final Storage storage = createStorage(storageRepository::markAsChanged);
            storageRepository.set(storageId, storage);
        }
        mainNode.setProvider(this);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_STORAGE_ID)) {
            setStorageId(tag.getUUID(TAG_STORAGE_ID));
        }
        super.load(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        configContainer.load(tag);
        filter.load(tag);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        if (storageId != null) {
            tag.putUUID(TAG_STORAGE_ID, storageId);
        }
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        configContainer.save(tag);
        filter.save(tag);
    }

    @Override
    public void setStorageId(final UUID storageId) {
        tryRemoveCurrentStorage(storageId);
        this.storageId = storageId;
        mainNode.onStorageChanged(0);
    }

    private void tryRemoveCurrentStorage(final UUID newStorageId) {
        if (level == null || storageId == null || storageId.equals(newStorageId)) {
            return;
        }
        // We got placed through NBT (#setLevel(Level) -> #load(CompoundTag)), or,
        // we got placed with an existing storage ID (#setLevel(Level) -> #setStorageId(UUID)).
        // Clean up the storage created earlier in #setLevel(Level).
        LOGGER.info("Updating storage ID from {} to {}. Removing old storage", storageId, newStorageId);
        final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        storageRepository.removeIfEmpty(storageId).ifPresentOrElse(
            storage -> LOGGER.info("Storage {} successfully removed", storageId),
            () -> LOGGER.warn("Storage {} could not be removed", storageId)
        );
    }

    @Override
    @Nullable
    public UUID getStorageId() {
        return storageId;
    }

    protected final ResourceContainer getFilterContainer() {
        return filter.getFilterContainer();
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeLong(mainNode.getStored());
        buf.writeLong(mainNode.getCapacity());
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Optional<Storage> resolve(final int index) {
        if (level == null || storageId == null) {
            return Optional.empty();
        }
        final StorageRepository storageRepository = PlatformApi.INSTANCE.getStorageRepository(level);
        return storageRepository.get(storageId);
    }
}
