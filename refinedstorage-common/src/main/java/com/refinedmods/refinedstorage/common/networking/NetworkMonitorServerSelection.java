package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.NetworkNodeDetailsChangedEvent;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsChangedEvent;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeTypeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

class NetworkMonitorServerSelection {
    private final NetworkMonitorBlockEntity networkMonitor;
    private final BiConsumer<@Nullable MonitorNodeId, NetworkNodeDetails> detailsListener;
    private final BiConsumer<MonitorNodeId, Object> nodeEventListener;
    @Nullable
    private Selection selection;

    NetworkMonitorServerSelection(final NetworkMonitorBlockEntity networkMonitor,
                                  final BiConsumer<@Nullable MonitorNodeId, NetworkNodeDetails> detailsListener,
                                  final BiConsumer<MonitorNodeId, Object> nodeEventListener) {
        this.networkMonitor = networkMonitor;
        this.detailsListener = detailsListener;
        this.nodeEventListener = nodeEventListener;
    }

    void setSelectedDevice(@Nullable final MonitorNodeId deviceId,
                           @Nullable final MonitorNodeTypeId deviceGroupId,
                           @Nullable final NetworkMonitorDeviceCategory deviceCategory) {
        removed();
        if (deviceId != null) {
            selection = new DeviceSelection(deviceId);
        } else if (deviceGroupId != null) {
            selection = new MergedSelection(
                () -> networkMonitor.getDeviceIds(deviceGroupId),
                deviceGroupId::equals
            );
        } else if (deviceCategory != null) {
            selection = new MergedSelection(
                () -> networkMonitor.getDeviceIds(deviceCategory),
                typeId -> networkMonitor.getDeviceCategory(typeId) == deviceCategory
            );
        } else {
            return;
        }
        selection.attach();
        selection.sendDetails();
    }

    void onNodeTracked(final MonitorNodeId id, final MonitorNodeTypeId typeId) {
        if (selection != null) {
            selection.onNodeTracked(id, typeId);
        }
    }

    void onNodeUntracked(final MonitorNodeId id) {
        if (selection != null) {
            selection.onNodeUntracked(id);
        }
    }

    void tick() {
        if (selection != null) {
            selection.tick();
        }
    }

    void removed() {
        if (selection != null) {
            selection.detach();
            selection = null;
        }
    }

    private interface Selection {
        void attach();

        void detach();

        void sendDetails();

        default void onNodeTracked(final MonitorNodeId id, final MonitorNodeTypeId typeId) {
            // no op
        }

        default void onNodeUntracked(final MonitorNodeId id) {
            // no op
        }

        default void tick() {
            // no op
        }
    }

    private class DeviceSelection implements Selection {
        private final MonitorNodeId deviceId;
        private final NetworkNodeListener listener;

        private DeviceSelection(final MonitorNodeId deviceId) {
            this.deviceId = deviceId;
            this.listener = event -> nodeEventListener.accept(deviceId, event);
        }

        @Override
        public void attach() {
            networkMonitor.addNodeListener(deviceId, listener);
        }

        @Override
        public void detach() {
            networkMonitor.removeNodeListener(deviceId, listener);
        }

        @Override
        public void sendDetails() {
            final NetworkNodeDetails details = networkMonitor.createDetails(deviceId);
            if (details != null) {
                detailsListener.accept(deviceId, details);
            }
        }
    }

    private class MergedSelection implements Selection {
        private final Supplier<Set<MonitorNodeId>> initialDeviceIds;
        private final Predicate<MonitorNodeTypeId> isPartOfSelection;
        private final Map<MonitorNodeId, NetworkNodeListener> listeners = new HashMap<>();
        private long stored;
        private long capacity;
        private boolean dirty;

        private MergedSelection(final Supplier<Set<MonitorNodeId>> initialDeviceIds,
                                final Predicate<MonitorNodeTypeId> isPartOfSelection) {
            this.initialDeviceIds = initialDeviceIds;
            this.isPartOfSelection = isPartOfSelection;
        }

        @Override
        public void attach() {
            initialDeviceIds.get().forEach(this::attach);
        }

        private void attach(final MonitorNodeId id) {
            final NetworkNodeListener listener = event -> onEvent(id, event);
            listeners.put(id, listener);
            networkMonitor.addNodeListener(id, listener);
        }

        private void onEvent(final MonitorNodeId id, final Object event) {
            switch (event) {
                case StorageContentsChangedEvent(var resource, long change, long nodeStored, long nodeCapacity) -> {
                    stored += change;
                    nodeEventListener.accept(id, new StorageContentsChangedEvent(resource, change, stored, capacity));
                }
                case NetworkNodeDetailsChangedEvent detailsChangedEvent -> {
                    nodeEventListener.accept(id, detailsChangedEvent);
                    dirty = true;
                }
                default -> nodeEventListener.accept(id, event);
            }
        }

        @Override
        public void detach() {
            listeners.forEach(networkMonitor::removeNodeListener);
            listeners.clear();
        }

        @Override
        public void sendDetails() {
            final NetworkNodeDetails details = networkMonitor.createDetails(listeners.keySet());
            if (details instanceof StorageContentsNetworkNodeDetails storageDetails) {
                stored = storageDetails.getStored();
                capacity = storageDetails.getCapacity();
            }
            dirty = false;
            detailsListener.accept(null, details);
        }

        @Override
        public void onNodeTracked(final MonitorNodeId id, final MonitorNodeTypeId typeId) {
            if (isPartOfSelection.test(typeId)) {
                attach(id);
                dirty = true;
            }
        }

        @Override
        public void onNodeUntracked(final MonitorNodeId id) {
            if (listeners.remove(id) != null) {
                dirty = true;
            }
        }

        @Override
        public void tick() {
            if (dirty) {
                sendDetails();
            }
        }
    }
}
