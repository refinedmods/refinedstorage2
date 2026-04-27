package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.storage.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public abstract class AbstractExternalStorageBlockEntity
    extends AbstractCableLikeBlockEntity<ExternalStorageNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExternalStorageBlockEntity.class);
    private static final String TAG_TRACKED_RESOURCES = "tr";

    private final FilterWithFuzzyMode filter;
    private final StorageConfigurationContainerImpl configContainer;
    private final ExternalStorageTrackedStorageRepository trackedStorageRepository =
        new ExternalStorageTrackedStorageRepository(this::setChanged);
    private final ExternalStorageWorkRate workRate = new ExternalStorageWorkRate();
    private boolean initialized;

    protected AbstractExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getExternalStorage(), pos, state, new ExternalStorageNetworkNode(
            Platform.INSTANCE.getConfig().getExternalStorage().getEnergyUsage(),
            System::currentTimeMillis
        ));
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            mainNetworkNode.getStorageConfiguration()::setFilters
        );
        mainNetworkNode.getStorageConfiguration().setNormalizer(filter.createNormalizer());
        mainNetworkNode.setTrackingRepository(trackedStorageRepository);
        this.configContainer = new StorageConfigurationContainerImpl(
            mainNetworkNode.getStorageConfiguration(),
            filter,
            this::setChanged,
            this::getRedstoneMode,
            this::setRedstoneMode
        );
    }

    @Override
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (level instanceof ServerLevel serverLevel) {
            LOGGER.debug("Reloading external storage @ {} as block state has changed", worldPosition);
            loadStorage(serverLevel);
        }
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        if (!initialized && level instanceof ServerLevel serverLevel) {
            LOGGER.debug("Triggering initial load of external storage {}", worldPosition);
            loadStorage(serverLevel);
            initialized = true;
        }
    }

    void loadStorage(final ServerLevel serverLevel) {
        final Direction direction = tryExtractDirection(getBlockState());
        LOGGER.debug("Loading storage for external storage with direction {} @ {}", direction, worldPosition);
        if (direction == null) {
            return;
        }
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        final Identifier target =
            BuiltInRegistries.BLOCK.getKey(serverLevel.getBlockState(sourcePosition).getBlock());

        RefinedStorageApi.INSTANCE.getExternalStorageProviderBlocks()
            .get(target).ifPresentOrElse(
                provider -> mainNetworkNode.initialize(provider.create(serverLevel, sourcePosition, incomingDirection)),
                () -> mainNetworkNode.initialize(new CompositeExternalStorageProvider(RefinedStorageApi.INSTANCE
                .getExternalStorageProviderFactories()
                .stream()
                .map(factory -> factory.create(serverLevel, sourcePosition, incomingDirection))
                .toList())));
    }

    @Override
    public void doWork() {
        super.doWork();
        if (workRate.canDoWork()) {
            final boolean hasChanges = mainNetworkNode.detectChanges();
            if (hasChanges) {
                LOGGER.debug("External storage @ {} has changed!", worldPosition);
                workRate.faster();
            } else {
                workRate.slower();
            }
        }
    }

    public void neighborChanged() {
        workRate.faster();
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_TRACKED_RESOURCES, ExternalStorageTrackedStorageRepository.CODEC,
            trackedStorageRepository.getTrackedResources());
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        filter.store(output);
        configContainer.store(output);
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        input.read(TAG_TRACKED_RESOURCES, ExternalStorageTrackedStorageRepository.CODEC)
            .ifPresent(trackedStorageRepository::load);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        filter.read(input);
        configContainer.read(input);
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

    void setPriority(final int priority) {
        mainNetworkNode.getStorageConfiguration().setInsertPriority(priority);
    }

    void setVoidExcess(final boolean voidExcess) {
        mainNetworkNode.getStorageConfiguration().setVoidExcess(voidExcess);
    }

    @Override
    public ResourceContainerData getMenuData() {
        return ResourceContainerData.of(filter.getFilterContainer());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ResourceContainerData> getMenuCodec() {
        return ResourceContainerData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.EXTERNAL_STORAGE);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExternalStorageContainerMenu(syncId, player, filter.getFilterContainer(), configContainer,
            p -> Container.stillValidBlockEntity(this, p));
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
