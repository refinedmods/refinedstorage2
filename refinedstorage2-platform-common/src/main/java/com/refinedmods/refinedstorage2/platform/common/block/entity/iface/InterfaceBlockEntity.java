package com.refinedmods.refinedstorage2.platform.common.block.entity.iface;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage2.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProviderImpl;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.HashMap;
import java.util.Map;
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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class InterfaceBlockEntity
    extends AbstractInternalNetworkNodeContainerBlockEntity<InterfaceNetworkNode>
    implements ExtendedMenuProvider, BlockEntityWithDrops {
    private static final String TAG_EXPORT_ITEMS = "ei";
    private static final int EXPORT_SLOTS = 9;

    private final FilterWithFuzzyMode filter;
    private final ExportedResourcesContainer exportedResources;
    private final Container exportedResourcesAsContainer;
    private final Map<StorageChannelType<?>, InterfaceExternalStorageProvider<?>> externalStorageProviders =
        new HashMap<>();

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
        addExternalStorageProviders();
    }

    private void addExternalStorageProviders() {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll().forEach(
            storageChannelType -> externalStorageProviders.put(
                storageChannelType,
                createExternalStorageProvider(storageChannelType)
            )
        );
    }

    private <T> InterfaceExternalStorageProviderImpl<T> createExternalStorageProvider(
        final PlatformStorageChannelType<T> storageChannelType
    ) {
        return new InterfaceExternalStorageProviderImpl<>(
            getNode(),
            storageChannelType
        );
    }

    public static ResourceContainer createFilterContainer() {
        return new ResourceContainerImpl(
            EXPORT_SLOTS,
            ResourceContainerType.FILTER_WITH_AMOUNT,
            InterfaceBlockEntity::getTransferQuota,
            PlatformApi.INSTANCE.getItemResourceFactory(),
            PlatformApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    public static ExportedResourcesContainer createExportedResourcesContainer(final FilterWithFuzzyMode filter) {
        return new ExportedResourcesContainer(EXPORT_SLOTS, filter);
    }

    static <T> long getTransferQuota(final ResourceTemplate<T> resourceTemplate) {
        if (resourceTemplate.storageChannelType() instanceof PlatformStorageChannelType<T> storageChannelType) {
            return storageChannelType.getInterfaceExportLimit(resourceTemplate.resource());
        }
        return 0;
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_EXPORT_ITEMS, exportedResources.toTag());
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_EXPORT_ITEMS)) {
            exportedResources.fromTag(tag.getCompound(TAG_EXPORT_ITEMS));
        }
        filter.load(tag);
        super.load(tag);
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    public Container getExportedResources() {
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
        return createTranslation("block", "interface");
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < exportedResourcesAsContainer.getContainerSize(); ++i) {
            drops.add(exportedResourcesAsContainer.getItem(i));
        }
        return drops;
    }

    @SuppressWarnings("unchecked")
    public <T> InterfaceExternalStorageProvider<T> getExternalStorageProvider(
        final StorageChannelType<T> storageChannelType
    ) {
        return (InterfaceExternalStorageProvider<T>) externalStorageProviders.get(storageChannelType);
    }
}
