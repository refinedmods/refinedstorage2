package com.refinedmods.refinedstorage.platform.common.iface;

import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProviderImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class InterfaceBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<InterfaceNetworkNode>
    implements NetworkNodeExtendedMenuProvider<InterfaceData>, BlockEntityWithDrops {
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
        this.mainNode.setTransferQuotaProvider(InterfaceBlockEntity::getTransferQuota);
        this.filter = FilterWithFuzzyMode.create(createFilterContainer(), this::setChanged);
        this.exportedResources = createExportedResourcesContainer(filter);
        this.exportedResources.setListener(this::setChanged);
        this.mainNode.setExportState(exportedResources);
        this.exportedResourcesAsContainer = exportedResources.toItemContainer();
        this.externalStorageProvider = new InterfaceExternalStorageProviderImpl(mainNode);
    }

    static ResourceContainer createFilterContainer() {
        return new ResourceContainerImpl(
            EXPORT_SLOTS,
            InterfaceBlockEntity::getTransferQuota,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ResourceContainer createFilterContainer(final InterfaceData interfaceData) {
        final ResourceContainer filterContainer = createFilterContainer();
        final ResourceContainerData resourceContainerData = interfaceData.filterContainerData();
        for (int i = 0; i < resourceContainerData.resources().size(); ++i) {
            final int ii = i;
            resourceContainerData.resources().get(i).ifPresent(resource -> filterContainer.set(ii, resource));
        }
        return filterContainer;
    }

    static ExportedResourcesContainer createExportedResourcesContainer(final FilterWithFuzzyMode filter) {
        return new ExportedResourcesContainer(EXPORT_SLOTS, filter);
    }

    static ResourceContainer createExportedResourcesContainer(final InterfaceData interfaceData,
                                                              final FilterWithFuzzyMode filter) {
        final ExportedResourcesContainer exportedResourcesContainer = createExportedResourcesContainer(filter);
        final ResourceContainerData resourceContainerData = interfaceData.exportedResourcesContainerData();
        for (int i = 0; i < resourceContainerData.resources().size(); ++i) {
            final int ii = i;
            resourceContainerData.resources().get(i).ifPresent(
                resource -> exportedResourcesContainer.set(ii, resource)
            );
        }
        return exportedResourcesContainer;
    }

    static long getTransferQuota(final ResourceKey resource) {
        if (resource instanceof PlatformResourceKey platformResource) {
            return platformResource.getInterfaceExportLimit();
        }
        return 0;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_EXPORT_ITEMS, exportedResources.toTag(provider));
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        filter.save(tag, provider);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_EXPORT_ITEMS)) {
            exportedResources.fromTag(tag.getCompound(TAG_EXPORT_ITEMS), provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
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
    public InterfaceData getMenuData() {
        return new InterfaceData(
            ResourceContainerData.of(filter.getFilterContainer()),
            ResourceContainerData.of(exportedResources)
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, InterfaceData> getMenuCodec() {
        return InterfaceData.STREAM_CODEC;
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

    InterfaceNetworkNode getInterface() {
        return mainNode;
    }
}
