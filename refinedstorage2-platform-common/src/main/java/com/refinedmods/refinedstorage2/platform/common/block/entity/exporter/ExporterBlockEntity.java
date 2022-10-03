package com.refinedmods.refinedstorage2.platform.common.block.entity.exporter;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling.ExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.CompositeExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<ExporterNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_SCHEDULING_MODE = "sm";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_RESOURCE_FILTER = "rf";
    private static final String TAG_UPGRADES = "u";

    private final ResourceFilterContainer resourceFilterContainer;
    private final UpgradeContainer upgradeContainer;
    private ExporterSchedulingModeSettings schedulingModeSettings = ExporterSchedulingModeSettings.FIRST_AVAILABLE;
    @Nullable
    private ExporterSchedulingMode schedulingMode;
    private boolean exactMode;
    private RateLimiter rateLimiter = createRateLimiter(0);

    public ExporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getExporter(),
            pos,
            state,
            new ExporterNetworkNode(calculateEnergyUsage(0, false))
        );
        this.setSchedulingMode(null, schedulingModeSettings);
        this.resourceFilterContainer = new ResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            9,
            this::resourceFilterContainerChanged
        );
        this.upgradeContainer = new UpgradeContainer(
            UpgradeDestinations.EXPORTER,
            PlatformApi.INSTANCE.getUpgradeRegistry(),
            this::upgradeContainerChanged
        );
    }

    // used to handle rotations
    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        updateTransferStrategy(serverLevel);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        updateTransferStrategy(serverLevel);
    }

    @Override
    public void doWork() {
        if (rateLimiter.tryAcquire()) {
            super.doWork();
        }
    }

    private void updateTransferStrategy(final ServerLevel serverLevel) {
        final Direction direction = getDirection();
        if (direction == null) {
            LOGGER.warn(
                "Could not extract direction from exporter block at {}, state is {}",
                worldPosition,
                getBlockState()
            );
            return;
        }
        final ExporterTransferStrategy strategy = createStrategy(serverLevel, direction);
        LOGGER.info("Initialized exporter at {} with strategy {}", worldPosition, strategy);
        getNode().setTransferStrategy(strategy);
    }

    private ExporterTransferStrategy createStrategy(final ServerLevel serverLevel,
                                                    final Direction direction) {
        final boolean hasStackUpgrade = hasStackUpgrade();
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.offset(direction.getNormal());
        final List<ExporterTransferStrategyFactory> factories =
            PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().getAll();
        final List<ExporterTransferStrategy> strategies = factories
            .stream()
            .map(factory -> factory.create(serverLevel, sourcePosition, incomingDirection, hasStackUpgrade, !exactMode))
            .toList();
        return new CompositeExporterTransferStrategy(strategies);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_RESOURCE_FILTER, resourceFilterContainer.toTag());
        tag.put(TAG_UPGRADES, upgradeContainer.createTag());
        if (schedulingMode != null && schedulingModeSettings != null) {
            tag.putInt(TAG_SCHEDULING_MODE, schedulingModeSettings.getId());
            schedulingModeSettings.writeToTag(tag, schedulingMode);
        }
        tag.putBoolean(TAG_EXACT_MODE, exactMode);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_SCHEDULING_MODE)) {
            setSchedulingMode(tag, ExporterSchedulingModeSettings.getById(tag.getInt(TAG_SCHEDULING_MODE)));
        }

        if (tag.contains(TAG_EXACT_MODE)) {
            this.exactMode = tag.getBoolean(TAG_EXACT_MODE);
        }

        if (tag.contains(TAG_RESOURCE_FILTER)) {
            resourceFilterContainer.load(tag.getCompound(TAG_RESOURCE_FILTER));
        }

        if (tag.contains(TAG_UPGRADES)) {
            upgradeContainer.fromTag(tag.getList(TAG_UPGRADES, Tag.TAG_COMPOUND));
        }

        initializeResourceFilter();
        initializeUpgrades();

        super.load(tag);
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

    public boolean isExactMode() {
        return exactMode;
    }

    public void setExactMode(final boolean exactMode) {
        this.exactMode = exactMode;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            updateTransferStrategy(serverLevel);
        }
    }

    private void resourceFilterContainerChanged() {
        initializeResourceFilter();
        setChanged();
    }

    private void initializeResourceFilter() {
        getNode().setTemplates(resourceFilterContainer.getTemplates());
    }

    private void upgradeContainerChanged() {
        initializeUpgrades();
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            updateTransferStrategy(serverLevel);
        }
    }

    private void initializeUpgrades() {
        final int amountOfSpeedUpgrades = upgradeContainer.countItem(Items.INSTANCE.getSpeedUpgrade());
        final boolean hasStackUpgrade = hasStackUpgrade();
        rateLimiter = createRateLimiter(amountOfSpeedUpgrades);
        getNode().setEnergyUsage(calculateEnergyUsage(amountOfSpeedUpgrades, hasStackUpgrade));
    }

    private boolean hasStackUpgrade() {
        return upgradeContainer.hasAnyOf(Set.of(Items.INSTANCE.getStackUpgrade()));
    }

    private static long calculateEnergyUsage(final int amountOfSpeedUpgrades, final boolean hasStackUpgrade) {
        return Platform.INSTANCE.getConfig().getExporter().getEnergyUsage()
            + (Platform.INSTANCE.getConfig().getUpgrade().getSpeedUpgradeEnergyUsage() * amountOfSpeedUpgrades)
            + (hasStackUpgrade ? Platform.INSTANCE.getConfig().getUpgrade().getStackUpgradeEnergyUsage() : 0L);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        resourceFilterContainer.writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "exporter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExporterContainerMenu(syncId, player, this, resourceFilterContainer, upgradeContainer);
    }
}
