package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageConfigurationProvider;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider, StorageConfigurationProvider, StorageConfiguration {
    private final FilterWithFuzzyMode filter;
    private final StorageConfigurationPersistence storageConfigurationPersistence;

    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private FilterMode filterMode = FilterMode.BLOCK;

    public ExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getExternalStorage(), pos, state, new SimpleNetworkNode(0)); // TODO
        this.filter = new FilterWithFuzzyMode(
            this::setChanged,
            value -> {
            },
            value -> {
            }
        );
        this.storageConfigurationPersistence = new StorageConfigurationPersistence(this);
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
        return priority;
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
        setChanged();
    }

    @Override
    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public void setFilterMode(final FilterMode filterMode) {
        this.filterMode = filterMode;
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
        return accessMode;
    }

    @Override
    public void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
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
