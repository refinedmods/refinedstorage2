package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceTransferResult;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.externalstorage.InterfaceExternalStorageProviderImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicators;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class InterfaceBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<InterfaceNetworkNode>
    implements NetworkNodeExtendedMenuProvider<InterfaceData> {
    private static final String TAG_EXPORT_ITEMS = "ei";
    private static final int EXPORT_SLOTS = 9;
    private static final String TAG_UPGRADES = "upgr";

    private final UpgradeContainer upgradeContainer;
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
        this.mainNetworkNode.setTransferQuotaProvider(InterfaceBlockEntity::getTransferQuota);
        this.upgradeContainer = new UpgradeContainer(1, UpgradeDestinations.INTERFACE, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getInterface().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            final boolean autocrafting = c.has(Items.INSTANCE.getAutocraftingUpgrade());
            mainNetworkNode.setOnMissingResources(autocrafting
                ? new InterfaceNetworkNode.AutocraftOnMissingResources()
                : InterfaceNetworkNode.OnMissingResources.EMPTY);
        }, this::setChanged);
        this.filter = FilterWithFuzzyMode.create(createFilterContainer(), this::setChanged);
        this.exportedResources = createExportedResourcesContainer(filter);
        this.exportedResources.setListener(this::setChanged);
        this.mainNetworkNode.setExportState(exportedResources);
        this.exportedResourcesAsContainer = exportedResources.toItemContainer();
        this.externalStorageProvider = new InterfaceExternalStorageProviderImpl(mainNetworkNode);
    }

    static ResourceContainer createFilterContainer() {
        return new ResourceContainerImpl(
            EXPORT_SLOTS,
            InterfaceBlockEntity::getTransferQuota,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
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
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_EXPORT_ITEMS, ResourceCodecs.CONTAINER_CONTENTS_CODEC,
            ResourceContainerContents.of(exportedResources));
        output.store(TAG_UPGRADES, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(upgradeContainer.getItems()));
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        filter.store(output);
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        input.read(TAG_EXPORT_ITEMS, ResourceCodecs.CONTAINER_CONTENTS_CODEC).ifPresent(exportedResources::load);
        input.read(TAG_UPGRADES, ItemContainerContents.CODEC).ifPresent(upgradeContainer::load);
        super.loadAdditional(input);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        filter.read(input);
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    void clearFilters() {
        filter.getFilterContainer().clear();
    }

    void setFilters(final List<ResourceAmount> filters) {
        for (int i = 0; i < filters.size(); i++) {
            filter.getFilterContainer().set(i, filters.get(i));
        }
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
            exportedResourcesAsContainer,
            upgradeContainer,
            getExportingIndicators()
        );
    }

    private ExportingIndicators getExportingIndicators() {
        return new ExportingIndicators(
            filter.getFilterContainer(),
            i -> toExportingIndicator(mainNetworkNode.getLastResult(i)),
            true
        );
    }

    private ExportingIndicator toExportingIndicator(@Nullable final InterfaceTransferResult result) {
        return switch (result) {
            case STORAGE_DOES_NOT_ACCEPT_RESOURCE -> ExportingIndicator.DESTINATION_DOES_NOT_ACCEPT_RESOURCE;
            case RESOURCE_MISSING -> ExportingIndicator.RESOURCE_MISSING;
            case AUTOCRAFTING_STARTED -> ExportingIndicator.AUTOCRAFTING_WAS_STARTED;
            case AUTOCRAFTING_MISSING_RESOURCES -> ExportingIndicator.AUTOCRAFTING_MISSING_RESOURCES;
            case null, default -> ExportingIndicator.NONE;
        };
    }

    @Override
    public InterfaceData getMenuData() {
        return new InterfaceData(
            ResourceContainerData.of(filter.getFilterContainer()),
            ResourceContainerData.of(exportedResources),
            getExportingIndicators().getAll()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, InterfaceData> getMenuCodec() {
        return InterfaceData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.INTERFACE);
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            final NonNullList<ItemStack> drops = NonNullList.create();
            for (int i = 0; i < exportedResourcesAsContainer.getContainerSize(); ++i) {
                drops.add(exportedResourcesAsContainer.getItem(i));
            }
            drops.addAll(upgradeContainer.getDrops());
            Containers.dropContents(level, pos, drops);
        }
    }

    InterfaceExternalStorageProvider getExternalStorageProvider() {
        return externalStorageProvider;
    }

    InterfaceNetworkNode getInterface() {
        return mainNetworkNode;
    }
}
