package com.refinedmods.refinedstorage2.platform.common.iface;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProviderImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class InterfaceBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<InterfaceNetworkNode>
    implements ExtendedMenuProvider, BlockEntityWithDrops {
    private static final String TAG_EXPORT_ITEMS = "ei";
    private static final int EXPORT_SLOTS = 9;

    private final FilterWithFuzzyMode filter;
    private final ExportedResourcesContainer exportedResources;
    private final Container exportedResourcesAsContainer;
    private final InterfaceExternalStorageProvider externalStorageProvider;

    public InterfaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getInterface(),
            pos,
            state,
            new InterfaceNetworkNode(Platform.INSTANCE.getConfig().getInterface().getEnergyUsage())
        );
        getNode().setTransferQuotaProvider(InterfaceBlockEntity::getTransferQuota);
        this.filter = FilterWithFuzzyMode.create(createFilterContainer(), this::setChanged);
        this.exportedResources = createExportedResourcesContainer(filter);
        this.exportedResources.setListener(this::setChanged);
        getNode().setExportState(exportedResources);
        this.exportedResourcesAsContainer = exportedResources.toItemContainer();
        this.externalStorageProvider = new InterfaceExternalStorageProviderImpl(getNode());
    }

    static ResourceContainer createFilterContainer() {
        return new ResourceContainerImpl(
            EXPORT_SLOTS,
            ResourceContainerType.FILTER_WITH_AMOUNT,
            InterfaceBlockEntity::getTransferQuota,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ExportedResourcesContainer createExportedResourcesContainer(final FilterWithFuzzyMode filter) {
        return new ExportedResourcesContainer(EXPORT_SLOTS, filter);
    }

    static long getTransferQuota(final ResourceKey resource) {
        if (resource instanceof PlatformResourceKey platformResource) {
            return platformResource.getInterfaceExportLimit();
        }
        return 0;
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_EXPORT_ITEMS, exportedResources.toTag());
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_EXPORT_ITEMS)) {
            exportedResources.fromTag(tag.getCompound(TAG_EXPORT_ITEMS));
        }
        super.load(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        filter.load(tag);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    public ExportedResourcesContainer getExportedResources() {
        return exportedResources;
    }

    public Container getExportedResourcesAsContainer() {
        return exportedResourcesAsContainer;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new InterfaceContainerMenu(
            syncId,
            player,
            this,
            filter.getFilterContainer(),
            exportedResources,
            exportedResourcesAsContainer
        );
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
        exportedResources.writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.INTERFACE;
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < exportedResourcesAsContainer.getContainerSize(); ++i) {
            drops.add(exportedResourcesAsContainer.getItem(i));
        }
        return drops;
    }

    InterfaceExternalStorageProvider getExternalStorageProvider() {
        return externalStorageProvider;
    }
}
