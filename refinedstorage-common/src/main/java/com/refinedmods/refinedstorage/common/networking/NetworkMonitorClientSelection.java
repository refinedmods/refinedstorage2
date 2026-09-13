package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilder;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilderImpl;
import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.storage.PlatformStorageContentsNetworkDetails;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;

import org.jspecify.annotations.Nullable;

class NetworkMonitorClientSelection {
    @Nullable
    private NetworkMonitorDeviceGroup deviceGroup;
    @Nullable
    private NetworkMonitorDevice device;
    @Nullable
    private NetworkMonitorDeviceCategory deviceCategory;
    @Nullable
    private NetworkNodeDetails details;
    @Nullable
    private ResourceRepository<GridResource> detailsRepository;

    @Nullable
    NetworkMonitorDeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    @Nullable
    NetworkMonitorDeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    @Nullable
    NetworkMonitorDevice getDevice() {
        return device;
    }

    @Nullable
    NetworkNodeDetails getDetails() {
        return details;
    }

    @Nullable
    ResourceRepository<GridResource> getDetailsRepository() {
        return detailsRepository;
    }

    void update(@Nullable final NetworkMonitorDeviceGroup newDeviceGroup,
                @Nullable final NetworkMonitorDeviceCategory newDeviceCategory,
                @Nullable final NetworkMonitorDevice newDevice) {
        this.deviceGroup = newDeviceGroup;
        this.deviceCategory = newDeviceCategory;
        this.device = newDevice;
        this.details = null;
        this.detailsRepository = null;
        notifyServer();
    }

    boolean removeDevice(final MonitorNodeId id) {
        if (device == null || !device.id().equals(id.id())) {
            return false;
        }
        this.device = null;
        this.details = null;
        this.detailsRepository = null;
        notifyServer();
        return true;
    }

    boolean removeDeviceGroup(final MonitorNodeTypeId id) {
        if (deviceGroup == null || !deviceGroup.id().equals(id.id())) {
            return false;
        }
        this.deviceGroup = null;
        notifyServer();
        return true;
    }

    boolean removeDeviceCategory(final @Nullable NetworkMonitorDeviceCategory category) {
        if (deviceCategory == null || !deviceCategory.equals(category)) {
            return false;
        }
        this.deviceCategory = null;
        notifyServer();
        return true;
    }

    private void notifyServer() {
        C2SPackets.sendNetworkMonitorSelectionUpdate(device == null ? null : device.id(),
            deviceGroup == null ? null : deviceGroup.id(), deviceCategory);
    }

    DetailsUpdateResult updateDetails(@Nullable final MonitorNodeId deviceId, final NetworkNodeDetails newDetails) {
        if (!isDetailsForCurrentSelection(deviceId)) {
            return DetailsUpdateResult.IGNORED;
        }
        final boolean areForCurrentSelection = details != null;
        this.details = newDetails;
        this.detailsRepository = createDetailsRepository(newDetails);
        return areForCurrentSelection ? DetailsUpdateResult.REFRESHED : DetailsUpdateResult.CHANGED;
    }

    void updateDetails(final MonitorNodeId deviceId, final long energyUsage, final boolean active) {
        if (details instanceof AbstractNetworkNodeDetails baseDetails
            && device != null
            && device.id().equals(deviceId.id())) {
            baseDetails.update(energyUsage, active);
        }
    }

    private boolean isDetailsForCurrentSelection(@Nullable final MonitorNodeId deviceId) {
        if (deviceId != null) {
            return device != null && device.id().equals(deviceId.id());
        }
        return device == null && (deviceGroup != null || deviceCategory != null);
    }

    public void updateResource(final ResourceKey resource, final long change, final long stored, final long capacity) {
        if (detailsRepository != null && change != 0) {
            detailsRepository.update(resource, change);
        }
        if (details == null) {
            return;
        }
        final StorageContentsNetworkNodeDetails storageDetails = PlatformStorageContentsNetworkDetails.unwrap(details);
        if (storageDetails != null) {
            storageDetails.updateStored(stored, capacity);
        }
    }

    @Nullable
    private static ResourceRepository<GridResource> createDetailsRepository(final NetworkNodeDetails details) {
        final StorageContentsNetworkNodeDetails storageDetails = PlatformStorageContentsNetworkDetails.unwrap(details);
        if (storageDetails == null) {
            return null;
        }
        final ResourceRepositoryBuilder<GridResource> builder = new ResourceRepositoryBuilderImpl<>(
            RefinedStorageApi.INSTANCE.getGridResourceRepositoryMapper(),
            GridSortingTypes.NAME.apply(resource -> null),
            GridSortingTypes.QUANTITY.apply(resource -> null)
        );
        storageDetails.getContents().forEach(content -> builder.addResource(content.resource(), content.amount()));
        final ResourceRepository<GridResource> repository = builder.build();
        repository.setSort(
            GridSortingTypes.QUANTITY.apply(resource -> null).apply(repository),
            SortingDirection.DESCENDING
        );
        return repository;
    }

    enum DetailsUpdateResult {
        /**
         * The details were for a selection that isn't current anymore.
         */
        IGNORED,
        /**
         * The details are the first details for the current selection.
         */
        CHANGED,
        /**
         * The details replace older details for the current selection.
         */
        REFRESHED
    }
}
