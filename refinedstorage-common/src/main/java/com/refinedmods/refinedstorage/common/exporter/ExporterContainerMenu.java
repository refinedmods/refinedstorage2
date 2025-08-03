package com.refinedmods.refinedstorage.common.exporter;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractSimpleFilterContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicatorListener;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicators;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;

import java.util.function.Predicate;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ExporterContainerMenu extends AbstractSimpleFilterContainerMenu<AbstractExporterBlockEntity>
    implements ExportingIndicatorListener {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "exporter.filter_help");

    private final ExportingIndicators indicators;
    private final Predicate<Player> stillValid;

    public ExporterContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final ExporterData data) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            playerInventory.player,
            data.resourceContainerData(),
            UpgradeDestinations.EXPORTER,
            FILTER_HELP
        );
        this.indicators = new ExportingIndicators(data.exportingIndicators());
        this.stillValid = p -> true;
    }

    ExporterContainerMenu(final int syncId,
                          final Player player,
                          final AbstractExporterBlockEntity exporter,
                          final ResourceContainer resourceContainer,
                          final UpgradeContainer upgradeContainer,
                          final ExportingIndicators indicators) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            exporter,
            FILTER_HELP
        );
        this.indicators = indicators;
        this.stillValid = p -> Container.stillValidBlockEntity(exporter, p);
    }

    ExportingIndicator getIndicator(final int idx) {
        return indicators.get(idx);
    }

    int getIndicators() {
        return indicators.size();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            indicators.detectChanges(serverPlayer);
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.SCHEDULING_MODE, SchedulingModeType.DEFAULT));
    }

    @Override
    protected void registerServerProperties(final AbstractExporterBlockEntity blockEntity) {
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            blockEntity::isFuzzyMode,
            blockEntity::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.SCHEDULING_MODE,
            blockEntity::getSchedulingModeType,
            blockEntity::setSchedulingModeType
        ));
    }

    @Override
    public void indicatorChanged(final int index, final ExportingIndicator indicator) {
        indicators.set(index, indicator);
    }
}
