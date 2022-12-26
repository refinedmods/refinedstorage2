package com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage;

import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageBlockEntity
    extends AbstractInternalNetworkNodeContainerBlockEntity<ExternalStorageNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LogManager.getLogger();
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
        this.filter = new FilterWithFuzzyMode(this::setChanged, getNode()::setFilterTemplates, value -> {
        });
        this.trackedStorageRepositoryProvider = new ExternalStorageTrackedStorageRepositoryProvider(
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
            this::setChanged
        );
        getNode().setNormalizer(filter.createNormalizer());
        getNode().initialize(
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
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
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (level instanceof ServerLevel serverLevel) {
            LOGGER.info("Reloading external storage @ {} as block state has changed", worldPosition);
            loadStorage(serverLevel);
        }
    }

    @Override
    protected void activenessChanged(final BlockState state,
                                     final boolean newActive,
                                     @Nullable final BooleanProperty activenessProperty) {
        super.activenessChanged(state, newActive, activenessProperty);
        if (!initialized && level instanceof ServerLevel serverLevel) {
            LOGGER.info("Triggering initial load of external storage {}", worldPosition);
            loadStorage(serverLevel);
            initialized = true;
        }
    }

    public void loadStorage(final ServerLevel serverLevel) {
        final Direction direction = getDirection();
        LOGGER.info("Loading storage for external storage with direction {} @ {}", direction, worldPosition);
        if (direction == null) {
            return;
        }
        getNode().initialize(new ExternalStorageProviderFactory() {
            @Override
            public <T> Optional<ExternalStorageProvider<T>> create(final StorageChannelType<T> channelType) {
                final Direction incomingDirection = direction.getOpposite();
                final BlockPos sourcePosition = worldPosition.relative(direction);
                return PlatformApi.INSTANCE
                    .getExternalStorageProviderFactories(channelType)
                    .stream()
                    .flatMap(factory -> factory.<T>create(serverLevel, sourcePosition, incomingDirection).stream())
                    .findFirst();
            }
        });
    }

    @Override
    public void doWork() {
        super.doWork();
        if (workRate.canDoWork()) {
            // TODO: some blocks constantly have changes ?!
            final boolean hasChanges = getNode().detectChanges();
            LOGGER.info("Detecting changes for external storage {}, changes = {}", worldPosition, hasChanges);
            if (hasChanges) {
                workRate.faster();
            } else {
                workRate.slower();
            }
        }
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        filter.save(tag);
        configContainer.save(tag);
        tag.put(TAG_TRACKED_RESOURCES, trackedStorageRepositoryProvider.toTag());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        filter.load(tag);
        configContainer.load(tag);
        trackedStorageRepositoryProvider.fromTag(tag.getList(TAG_TRACKED_RESOURCES, Tag.TAG_COMPOUND));
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "external_storage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExternalStorageContainerMenu(syncId, player, filter.getFilterContainer(), configContainer);
    }
}
