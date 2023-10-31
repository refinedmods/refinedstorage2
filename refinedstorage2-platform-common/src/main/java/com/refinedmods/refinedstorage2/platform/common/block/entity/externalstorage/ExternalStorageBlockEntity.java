package com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalStorageBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<ExternalStorageNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalStorageBlockEntity.class);
    private static final String TAG_TRACKED_RESOURCES = "tr";

    private final FilterWithFuzzyMode filter;
    private final StorageConfigurationContainerImpl configContainer;
    private final ExternalStorageTrackedStorageRepositoryProvider trackedStorageRepositoryProvider;
    private final WorkRate workRate = new WorkRate();
    private boolean initialized;

    public ExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getExternalStorage(), pos, state, new ExternalStorageNetworkNode(
            Platform.INSTANCE.getConfig().getExternalStorage().getEnergyUsage()
        ));
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueTemplates(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            templates -> getNode().setFilterTemplates(templates)
        );
        this.trackedStorageRepositoryProvider = new ExternalStorageTrackedStorageRepositoryProvider(
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
            this::setChanged
        );
        getNode().setNormalizer(filter.createNormalizer());
        getNode().initialize(
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll(),
            System::currentTimeMillis,
            trackedStorageRepositoryProvider
        );
        this.configContainer = new StorageConfigurationContainerImpl(
            getNode(),
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
    protected void activenessChanged(final BlockState state,
                                     final boolean newActive,
                                     @Nullable final BooleanProperty activenessProperty) {
        super.activenessChanged(state, newActive, activenessProperty);
        if (!initialized && level instanceof ServerLevel serverLevel) {
            LOGGER.debug("Triggering initial load of external storage {}", worldPosition);
            loadStorage(serverLevel);
            initialized = true;
        }
    }

    public void loadStorage(final ServerLevel serverLevel) {
        final Direction direction = getDirection();
        LOGGER.debug("Loading storage for external storage with direction {} @ {}", direction, worldPosition);
        if (direction == null) {
            return;
        }
        getNode().initialize(new ExternalStorageProviderFactory() {
            @Override
            public <T> Optional<ExternalStorageProvider<T>> create(final StorageChannelType<T> channelType) {
                final Direction incomingDirection = direction.getOpposite();
                final BlockPos sourcePosition = worldPosition.relative(direction);
                return PlatformApi.INSTANCE
                    .getExternalStorageProviderFactories()
                    .stream()
                    .flatMap(factory -> factory.<T>create(serverLevel, sourcePosition, incomingDirection, channelType)
                        .stream())
                    .findFirst();
            }
        });
    }

    @Override
    public void doWork() {
        super.doWork();
        if (workRate.canDoWork()) {
            final boolean hasChanges = getNode().detectChanges();
            if (hasChanges) {
                LOGGER.debug("External storage @ {} has changed!", worldPosition);
                workRate.faster();
            } else {
                workRate.slower();
            }
        }
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_TRACKED_RESOURCES, trackedStorageRepositoryProvider.toTag());
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        filter.save(tag);
        configContainer.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        trackedStorageRepositoryProvider.fromTag(tag.getList(TAG_TRACKED_RESOURCES, Tag.TAG_COMPOUND));
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        filter.load(tag);
        configContainer.load(tag);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.EXTERNAL_STORAGE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExternalStorageContainerMenu(syncId, player, filter.getFilterContainer(), configContainer);
    }
}
