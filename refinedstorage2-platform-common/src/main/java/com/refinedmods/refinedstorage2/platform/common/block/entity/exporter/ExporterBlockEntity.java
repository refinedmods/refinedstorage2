package com.refinedmods.refinedstorage2.platform.common.block.entity.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.CompositeExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractSchedulingNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterBlockEntity
    extends AbstractSchedulingNetworkNodeContainerBlockEntity<ExporterNetworkNode, ExporterNetworkNode.TaskContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExporterBlockEntity.class);

    public ExporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getExporter(),
            pos,
            state,
            new ExporterNetworkNode(0),
            UpgradeDestinations.EXPORTER
        );
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        final ExporterTransferStrategy strategy = createStrategy(level, direction);
        LOGGER.debug("Initialized exporter at {} with strategy {}", worldPosition, strategy);
        getNode().setTransferStrategy(strategy);
    }

    private ExporterTransferStrategy createStrategy(final ServerLevel serverLevel, final Direction direction) {
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        final List<ExporterTransferStrategyFactory> factories =
            PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().getAll();
        final List<ExporterTransferStrategy> strategies = factories
            .stream()
            .map(factory -> factory.create(
                serverLevel,
                sourcePosition,
                incomingDirection,
                this::hasUpgrade,
                filter.isFuzzyMode()
            ))
            .toList();
        return new CompositeExporterTransferStrategy(strategies);
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getExporter().getEnergyUsage();
        getNode().setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "exporter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExporterContainerMenu(syncId, player, this, filter.getFilterContainer(), upgradeContainer);
    }

    @Override
    protected void setTaskExecutor(final TaskExecutor<ExporterNetworkNode.TaskContext> taskExecutor) {
        getNode().setTaskExecutor(taskExecutor);
    }

    @Override
    protected void setFilterTemplates(final List<Object> templates) {
        getNode().setFilterTemplates(templates);
    }
}
