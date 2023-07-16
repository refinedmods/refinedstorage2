package com.refinedmods.refinedstorage2.platform.common.block.entity.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.CompositeExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractSchedulingNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.item.RegulatorUpgradeItem;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterBlockEntity
    extends AbstractSchedulingNetworkNodeContainerBlockEntity<ExporterNetworkNode, ExporterNetworkNode.TaskContext>
    implements AmountOverride, UpgradeState {
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
                this,
                this,
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

    @Override
    public <T> long overrideAmount(final T resource, final long amount, final LongSupplier currentAmount) {
        if (!hasUpgrade(Items.INSTANCE.getRegulatorUpgrade())) {
            return amount;
        }
        return overrideAmountForRegulatorUpgrade(resource, amount, currentAmount);
    }

    private <T> long overrideAmountForRegulatorUpgrade(final T resource,
                                                       final long amount,
                                                       final LongSupplier currentAmount) {
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            final ItemStack upgradeStack = upgradeContainer.getItem(i);
            if (!(upgradeStack.getItem() instanceof RegulatorUpgradeItem regulatorUpgrade)) {
                continue;
            }
            final OptionalLong desiredAmount = regulatorUpgrade.getDesiredAmount(upgradeStack, resource);
            if (desiredAmount.isPresent()) {
                return getAmountStillNeeded(amount, currentAmount.getAsLong(), desiredAmount.getAsLong());
            }
        }
        return amount;
    }

    private long getAmountStillNeeded(final long amount, final long currentAmount, final long desiredAmount) {
        final long stillNeeding = desiredAmount - currentAmount;
        if (stillNeeding <= 0) {
            return 0;
        }
        return Math.min(stillNeeding, amount);
    }
}
