package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.importer.CompositeImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.List;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ImporterBlockEntity
    extends AbstractUpgradeableLevelInteractingNetworkNodeContainerBlockEntity<ImporterNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_FUZZY_MODE = "fm";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private final ResourceFilterContainer resourceFilterContainer;
    private boolean fuzzyMode;

    public ImporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getImporter(),
            pos,
            state,
            new ImporterNetworkNode(0),
            UpgradeDestinations.IMPORTER
        );
        getNode().setNormalizer(value -> FuzzyModeNormalizer.tryNormalize(fuzzyMode, value));
        this.resourceFilterContainer = new ResourceFilterContainer(
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            9,
            this::resourceFilterContainerChanged
        );
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        final CompositeImporterTransferStrategy strategy = createStrategy(level, direction);
        LOGGER.info("Initialized importer at {} with strategy {}", worldPosition, strategy);
        getNode().setTransferStrategy(strategy);
    }

    private CompositeImporterTransferStrategy createStrategy(final ServerLevel serverLevel, final Direction direction) {
        final boolean hasStackUpgrade = hasStackUpgrade();
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.offset(direction.getNormal());
        final List<ImporterTransferStrategyFactory> factories =
            PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().getAll();
        final List<ImporterTransferStrategy> strategies = factories
            .stream()
            .map(factory -> factory.create(serverLevel, sourcePosition, incomingDirection, hasStackUpgrade))
            .toList();
        return new CompositeImporterTransferStrategy(strategies);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_RESOURCE_FILTER, resourceFilterContainer.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getNode().getFilterMode()));
        tag.putBoolean(TAG_FUZZY_MODE, fuzzyMode);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_FILTER_MODE)) {
            getNode().setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }

        if (tag.contains(TAG_FUZZY_MODE)) {
            this.fuzzyMode = tag.getBoolean(TAG_FUZZY_MODE);
        }

        if (tag.contains(TAG_RESOURCE_FILTER)) {
            resourceFilterContainer.load(tag.getCompound(TAG_RESOURCE_FILTER));
        }
        initializeResourceFilter();

        super.load(tag);
    }

    public boolean isFuzzyMode() {
        return fuzzyMode;
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        initializeResourceFilter();
        setChanged();
    }

    public FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    public void setFilterMode(final FilterMode mode) {
        getNode().setFilterMode(mode);
        setChanged();
    }

    private void resourceFilterContainerChanged() {
        initializeResourceFilter();
        setChanged();
    }

    private void initializeResourceFilter() {
        getNode().setFilterTemplates(resourceFilterContainer.getUniqueTemplates());
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getImporter().getEnergyUsage();
        getNode().setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        resourceFilterContainer.writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "importer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ImporterContainerMenu(syncId, player, this, resourceFilterContainer, upgradeContainer);
    }
}
