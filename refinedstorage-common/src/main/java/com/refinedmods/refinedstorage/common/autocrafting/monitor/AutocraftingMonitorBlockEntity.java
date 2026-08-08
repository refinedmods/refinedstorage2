package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.PlatformNetworkNodeTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class AutocraftingMonitorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<AutocraftingMonitorData>, AutocraftingMonitor {
    private final Set<AutocraftingMonitorWatcher> watchers = new HashSet<>();

    public AutocraftingMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getAutocraftingMonitor(), pos, state, new SimpleNetworkNode(
            PlatformNetworkNodeTypes.AUTOCRAFTING_MONITOR,
            Platform.INSTANCE.getConfig().getAutocraftingMonitor().getEnergyUsage()
        ));
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        watchers.forEach(watcher -> watcher.activeChanged(newActive));
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.AUTOCRAFTING_MONITOR);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    public AutocraftingMonitorData getMenuData() {
        return new AutocraftingMonitorData(getStatuses(), isAutocraftingMonitorActive());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, AutocraftingMonitorData> getMenuCodec() {
        return AutocraftingMonitorData.STREAM_CODEC;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new AutocraftingMonitorContainerMenu(syncId, player, this);
    }

    private Optional<AutocraftingNetworkComponent> getAutocrafting() {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return Optional.empty();
        }
        return Optional.of(network.getComponent(AutocraftingNetworkComponent.class));
    }

    @Override
    public List<TaskStatus> getStatuses() {
        return getAutocrafting().map(AutocraftingNetworkComponent::getStatuses).orElse(Collections.emptyList());
    }

    @Override
    public void addListener(final TaskStatusListener listener) {
        getAutocrafting().ifPresent(autocrafting -> autocrafting.addListener(listener));
    }

    @Override
    public void removeListener(final TaskStatusListener listener) {
        getAutocrafting().ifPresent(autocrafting -> autocrafting.removeListener(listener));
    }

    @Override
    public void cancel(final TaskId taskId) {
        getAutocrafting().ifPresent(autocrafting -> autocrafting.cancel(taskId));
    }

    @Override
    public void cancelAll() {
        getAutocrafting().ifPresent(AutocraftingNetworkComponent::cancelAll);
    }

    @Override
    public void addWatcher(final AutocraftingMonitorWatcher watcher) {
        watchers.add(watcher);
    }

    @Override
    public void removeWatcher(final AutocraftingMonitorWatcher watcher) {
        watchers.remove(watcher);
    }

    @Override
    public boolean isAutocraftingMonitorActive() {
        return mainNetworkNode.isActive();
    }
}
