package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class NetworkMonitorContainerMenu extends AbstractBaseContainerMenu implements ScreenSizeListener {
    private final Predicate<Player> stillValid;
    private final List<NetworkMonitorDeviceGroup> deviceGroups = new ArrayList<>();
    @Nullable
    private NetworkMonitorDeviceGroup currentDeviceGroup;
    @Nullable
    private NetworkMonitorDevice currentDevice;
    @Nullable
    private NetworkNodeDetails currentDetails;
    @Nullable
    private Consumer<NetworkNodeDetails> detailsListener;

    public NetworkMonitorContainerMenu(final int syncId, final NetworkMonitorData data) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> true;
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    public NetworkMonitorContainerMenu(final int syncId, final NetworkMonitorBlockEntity networkMonitor) {
        super(Menus.INSTANCE.getNetworkMonitor(), syncId);
        this.stillValid = p -> Container.stillValidBlockEntity(networkMonitor, p);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            networkMonitor::getRedstoneMode,
            networkMonitor::setRedstoneMode
        ));
    }

    List<NetworkMonitorDeviceGroup> getDeviceGroups() {
        return deviceGroups;
    }

    @Nullable
    NetworkMonitorDeviceGroup getCurrentDeviceGroup() {
        return currentDeviceGroup;
    }

    @Nullable
    NetworkMonitorDevice getCurrentDevice() {
        return currentDevice;
    }

    @Nullable
    NetworkNodeDetails getCurrentDetails() {
        return currentDetails;
    }

    void setCurrentDeviceGroup(final NetworkMonitorDeviceGroup group) {
        this.currentDeviceGroup = group;
        this.currentDevice = null;
        this.currentDetails = null;
    }

    void setCurrentDevice(final NetworkMonitorDeviceGroup group, final NetworkMonitorDevice device) {
        this.currentDeviceGroup = group;
        this.currentDevice = device;
        this.currentDetails = null;
    }

    void setDetailsListener(final Consumer<NetworkNodeDetails> detailsListener) {
        this.detailsListener = detailsListener;
    }

    boolean isActive() {
        return true;
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.stillValid.test(player);
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        // no op
    }
}
