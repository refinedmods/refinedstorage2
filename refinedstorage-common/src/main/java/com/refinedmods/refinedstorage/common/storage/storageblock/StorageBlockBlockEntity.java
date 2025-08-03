package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockData;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockProvider;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.storage.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageBlockBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<StorageNetworkNode>
    implements NetworkNodeExtendedMenuProvider<StorageBlockData>, StorageBlockEntity,
    AbstractStorageContainerNetworkNode.Provider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlockBlockEntity.class);

    private static final String TAG_STORAGE_ID = "sid";

    protected final StorageConfigurationContainerImpl configContainer;
    private final FilterWithFuzzyMode filter;
    private final Component displayName;
    private final StorageBlockProvider storageBlockProvider;

    @Nullable
    private UUID storageId;

    public StorageBlockBlockEntity(final BlockPos pos,
                                   final BlockState state,
                                   final StorageBlockProvider provider) {
        super(provider.getBlockEntityType(), pos, state, new StorageNetworkNode(provider.getEnergyUsage(), 0, 1));
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(provider.getResourceFactory()),
            this::setChanged,
            this::setFilters
        );
        this.configContainer = new StorageConfigurationContainerImpl(
            mainNetworkNode.getStorageConfiguration(),
            filter,
            this::setChanged,
            this::getRedstoneMode,
            this::setRedstoneMode
        );
        mainNetworkNode.getStorageConfiguration().setNormalizer(filter.createNormalizer());
        this.displayName = provider.getDisplayName();
        this.storageBlockProvider = provider;
    }

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
            final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getStorageRepository(level);
            final SerializableStorage storage = storageBlockProvider.createStorage(storageRepository::markAsChanged);
            storageRepository.set(storageId, storage);
        }
        mainNetworkNode.setProvider(this);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_STORAGE_ID)) {
            setStorageId(tag.getUUID(TAG_STORAGE_ID));
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        configContainer.load(tag);
        filter.load(tag, provider);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (storageId != null) {
            tag.putUUID(TAG_STORAGE_ID, storageId);
        }
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        configContainer.save(tag);
        filter.save(tag, provider);
    }

    @Override
    public void setStorageId(final UUID storageId) {
        tryRemoveCurrentStorage(storageId);
        this.storageId = storageId;
        mainNetworkNode.onStorageChanged(0);
    }

    private void tryRemoveCurrentStorage(final UUID newStorageId) {
        if (level == null || storageId == null || storageId.equals(newStorageId)) {
            return;
        }
        // We got placed through NBT (#setLevel(Level) -> #load(CompoundTag)), or,
        // we got placed with an existing storage ID (#setLevel(Level) -> #setStorageId(UUID)).
        // Clean up the storage created earlier in #setLevel(Level).
        LOGGER.info("Updating storage ID from {} to {}. Removing old storage", storageId, newStorageId);
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getStorageRepository(level);
        storageRepository.removeIfEmpty(storageId).ifPresentOrElse(
            storage -> LOGGER.info("Storage {} successfully removed", storageId),
            () -> LOGGER.warn("Storage {} could not be removed", storageId)
        );
    }

    void setFilters(final Set<ResourceKey> filters) {
        mainNetworkNode.getStorageConfiguration().setFilters(filters);
    }

    void setFilterMode(final FilterMode mode) {
        mainNetworkNode.getStorageConfiguration().setFilterMode(mode);
        setChanged();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    void setAccessMode(final AccessMode accessMode) {
        mainNetworkNode.getStorageConfiguration().setAccessMode(accessMode);
    }

    void setVoidExcess(final boolean voidExcess) {
        mainNetworkNode.getStorageConfiguration().setVoidExcess(voidExcess);
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
    public StorageBlockData getMenuData() {
        return new StorageBlockData(
            mainNetworkNode.getStored(),
            mainNetworkNode.getCapacity(),
            ResourceContainerData.of(filter.getFilterContainer()).resources()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, StorageBlockData> getMenuCodec() {
        return RefinedStorageApi.INSTANCE.getStorageBlockDataStreamCodec();
    }

    @Override
    public Optional<Storage> resolve(final int index) {
        if (level == null || storageId == null) {
            return Optional.empty();
        }
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getStorageRepository(level);
        return storageRepository.get(storageId).map(Storage.class::cast);
    }

    @Override
    public Component getName() {
        return overrideName(displayName);
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StorageBlockContainerMenu(
            storageBlockProvider.getMenuType(),
            syncId,
            player,
            filter.getFilterContainer(),
            configContainer,
            p -> Container.stillValidBlockEntity(this, p)
        );
    }
}
