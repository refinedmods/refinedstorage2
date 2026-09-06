package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.monitor.MonitorNodeId;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

class NetworkMonitorContainerSelection {
    private final NetworkMonitorBlockEntity networkMonitor;
    private final Consumer<NetworkNodeDetails> detailsConsumer;
    private NetworkMonitorContainerSelection.@Nullable SelectedDevice selectedDevice;

    NetworkMonitorContainerSelection(final NetworkMonitorBlockEntity networkMonitor,
                                     final Consumer<NetworkNodeDetails> detailsConsumer) {
        this.networkMonitor = networkMonitor;
        this.detailsConsumer = detailsConsumer;
    }

    void setSelectedDevice(@Nullable final MonitorNodeId deviceId) {
        if (selectedDevice != null) {
            selectedDevice.detach();
            selectedDevice = null;
        }
        if (deviceId == null) {
            return;
        }
        selectedDevice = new SelectedDevice(deviceId, event -> sendDetails(deviceId));
        selectedDevice.attach();
        sendDetails(deviceId);
    }

    private void sendDetails(final MonitorNodeId deviceId) {
        final NetworkNodeDetails details = networkMonitor.createDetails(deviceId);
        if (details == null) {
            return;
        }
        detailsConsumer.accept(details);
    }

    void removed() {
        if (selectedDevice != null) {
            selectedDevice.detach();
            selectedDevice = null;
        }
    }

    private class SelectedDevice {
        private final MonitorNodeId deviceId;
        private final NetworkNodeListener listener;

        private SelectedDevice(final MonitorNodeId deviceId, final NetworkNodeListener listener) {
            this.deviceId = deviceId;
            this.listener = listener;
        }

        private void attach() {
            networkMonitor.addNodeListener(deviceId, listener);
        }

        private void detach() {
            networkMonitor.removeNodeListener(deviceId, listener);
        }
    }
}
