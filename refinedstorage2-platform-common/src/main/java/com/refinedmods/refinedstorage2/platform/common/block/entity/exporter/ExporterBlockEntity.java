package com.refinedmods.refinedstorage2.platform.common.block.entity.exporter;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.strategy.CompositeExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling.ExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractUpgradeableNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterBlockEntity
    extends AbstractUpgradeableNetworkNodeContainerBlockEntity<ExporterNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExporterBlockEntity.class);

    private static final String TAG_SCHEDULING_MODE = "sm";

    private final FilterWithFuzzyMode filter;

    private ExporterSchedulingModeSettings schedulingModeSettings = ExporterSchedulingModeSettings.FIRST_AVAILABLE;
    @Nullable
    private ExporterSchedulingMode schedulingMode;

    public ExporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getExporter(),
            pos,
            state,
            new ExporterNetworkNode(0),
            UpgradeDestinations.EXPORTER
        );
        this.filter = new FilterWithFuzzyMode(this::setChanged, value -> {
        }, getNode()::setTemplates);
        this.setSchedulingMode(null, schedulingModeSettings);
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        final ExporterTransferStrategy strategy = createStrategy(level, direction);
        LOGGER.info("Initialized exporter at {} with strategy {}", worldPosition, strategy);
        getNode().setTransferStrategy(strategy);
    }

    private ExporterTransferStrategy createStrategy(final ServerLevel serverLevel, final Direction direction) {
        final boolean hasStackUpgrade = hasStackUpgrade();
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
                hasStackUpgrade,
                filter.isFuzzyMode()
            ))
            .toList();
        return new CompositeExporterTransferStrategy(strategies);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        if (schedulingMode != null) {
            tag.putInt(TAG_SCHEDULING_MODE, schedulingModeSettings.getId());
            schedulingModeSettings.writeToTag(tag, schedulingMode);
        }
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_SCHEDULING_MODE)) {
            setSchedulingMode(tag, ExporterSchedulingModeSettings.getById(tag.getInt(TAG_SCHEDULING_MODE)));
        }

        filter.load(tag);

        super.load(tag);
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getExporter().getEnergyUsage();
        getNode().setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
    }

    public void setSchedulingMode(final ExporterSchedulingModeSettings modeSettings) {
        setSchedulingMode(null, modeSettings);
        setChanged();
    }

    private void setSchedulingMode(@Nullable final CompoundTag tag,
                                   final ExporterSchedulingModeSettings modeSettings) {
        this.schedulingModeSettings = modeSettings;
        this.schedulingMode = modeSettings.create(tag, new Randomizer() {
            @Override
            public <T> void shuffle(final List<T> list) {
                Collections.shuffle(list, new Random());
            }
        }, this::setChanged);
        getNode().setSchedulingMode(schedulingMode);
    }

    public ExporterSchedulingModeSettings getSchedulingMode() {
        return schedulingModeSettings;
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

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "exporter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExporterContainerMenu(syncId, player, this, filter.getFilterContainer(), upgradeContainer);
    }
}
