package com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractLevelInteractingNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.StorageConfigurationPersistence;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageConfigurationProvider;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageBlockEntity
    extends AbstractLevelInteractingNetworkNodeContainerBlockEntity<ExternalStorageNetworkNode>
    implements ExtendedMenuProvider, StorageConfigurationProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FilterWithFuzzyMode filter;
    private final StorageConfigurationPersistence storageConfigurationPersistence;
    private final WorkRate workRate = new WorkRate();

    public ExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getExternalStorage(), pos, state, new ExternalStorageNetworkNode(
            Platform.INSTANCE.getConfig().getExternalStorage().getEnergyUsage(),
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
        ));
        this.filter = new FilterWithFuzzyMode(this::setChanged, getNode()::setFilterTemplates, value -> {
        });
        getNode().setNormalizer(filter.createNormalizer());
        this.storageConfigurationPersistence = new StorageConfigurationPersistence(getNode());
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        getNode().initialize(new ExternalStorageProviderFactory() {
            @Override
            public <T> Optional<ExternalStorageProvider<T>> create(final StorageChannelType<T> channelType) {
                final Direction incomingDirection = direction.getOpposite();
                final BlockPos sourcePosition = worldPosition.relative(direction);
                return PlatformApi.INSTANCE
                    .getExternalStorageProviderFactory(channelType)
                    .map(factory -> factory.create(level, sourcePosition, incomingDirection));
            }
        });
    }

    @Override
    public void doWork() {
        super.doWork();
        if (workRate.canDoWork()) {
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
        storageConfigurationPersistence.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        filter.load(tag);
        storageConfigurationPersistence.load(tag);
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
    public void setFilterMode(final FilterMode filterMode) {
        getNode().setFilterMode(filterMode);
        setChanged();
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
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
        return new ExternalStorageContainerMenu(syncId, player, filter.getFilterContainer(), this);
    }
}
