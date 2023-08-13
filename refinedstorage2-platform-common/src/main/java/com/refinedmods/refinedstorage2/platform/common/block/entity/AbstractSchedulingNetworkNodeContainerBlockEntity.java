package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSchedulingNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode, C>
    extends AbstractUpgradeableNetworkNodeContainerBlockEntity<T>
    implements ExtendedMenuProvider {
    protected final FilterWithFuzzyMode filter;
    private final SchedulingMode<C> schedulingMode;

    protected AbstractSchedulingNetworkNodeContainerBlockEntity(
        final BlockEntityType<?> type,
        final BlockPos pos,
        final BlockState state,
        final T node,
        final UpgradeDestinations destination
    ) {
        super(type, pos, state, node, destination);
        this.schedulingMode = new SchedulingMode<>(this::setChanged, this::setTaskExecutor);
        this.filter = FilterWithFuzzyMode.createAndListenForTemplates(
            ResourceContainer.createForFilter(),
            this::setChanged,
            templates -> setFilterTemplates(
                templates.stream().map(ResourceTemplate::resource).collect(Collectors.toList())
            )
        );
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        schedulingMode.writeToTag(tag);
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        schedulingMode.load(tag);
        filter.load(tag);
        super.load(tag);
    }

    public void setSchedulingModeType(final SchedulingModeType type) {
        schedulingMode.setType(type);
    }

    public SchedulingModeType getSchedulingModeType() {
        return schedulingMode.getType();
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    protected abstract void setTaskExecutor(TaskExecutor<C> taskExecutor);

    protected abstract void setFilterTemplates(List<Object> templates);
}
