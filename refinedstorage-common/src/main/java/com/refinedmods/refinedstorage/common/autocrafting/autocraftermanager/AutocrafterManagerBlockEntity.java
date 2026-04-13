package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.autocrafting.Autocrafter;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class AutocrafterManagerBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<AutocrafterManagerData> {
    private final Set<AutocrafterManagerWatcher> watchers = new HashSet<>();

    public AutocrafterManagerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getAutocrafterManager(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getAutocrafterManager().getEnergyUsage()
        ));
    }

    void addWatcher(final AutocrafterManagerWatcher watcher) {
        watchers.add(watcher);
    }

    void removeWatcher(final AutocrafterManagerWatcher watcher) {
        watchers.remove(watcher);
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        watchers.forEach(watcher -> watcher.activeChanged(newActive));
    }

    @Override
    public Component getName() {
        return ContentNames.AUTOCRAFTER_MANAGER;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    public AutocrafterManagerData getMenuData() {
        return new AutocrafterManagerData(
            getGroups().stream().map(AutocrafterManagerData.Group::of).toList(),
            mainNetworkNode.isActive()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, AutocrafterManagerData> getMenuCodec() {
        return AutocrafterManagerData.STREAM_CODEC;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new AutocrafterManagerContainerMenu(syncId, inventory, this, getGroups());
    }

    private Stream<Autocrafter> getAutocrafters() {
        final Network mainNetwork = mainNetworkNode.getNetwork();
        if (mainNetwork == null) {
            return Stream.empty();
        }
        return mainNetwork.getComponent(GraphNetworkComponent.class)
            .getContainers(Autocrafter.class)
            .stream()
            .sorted(Comparator.comparing(Autocrafter::getLocalPosition));
    }

    private List<Group> getGroups() {
        return getAutocrafters()
            .collect(Collectors.groupingBy(a -> a.getAutocrafterName().getString()))
            .entrySet()
            .stream()
            .map(entry -> new Group(
                entry.getKey(),
                entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(Autocrafter::getLocalPosition))
                    .map(SubGroup::of)
                    .toList()
            ))
            .sorted(Comparator.comparing(group -> group.name))
            .toList();
    }

    record Group(String name, List<SubGroup> subGroups) {
    }

    record SubGroup(Container container, boolean visibleToTheAutocrafterManager, boolean full) {
        private static SubGroup of(final Autocrafter autocrafter) {
            final Container container = autocrafter.getPatternContainer();
            final boolean full = isFull(container);
            return new SubGroup(container, autocrafter.isVisibleToTheAutocrafterManager(), full);
        }

        private static boolean isFull(final Container container) {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                if (container.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
