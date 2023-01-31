package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterSchedulingModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ExporterContainerMenu extends AbstractSimpleFilterContainerMenu<ExporterBlockEntity> {
    public ExporterContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            playerInventory.player,
            buf,
            UpgradeDestinations.EXPORTER
        );
    }

    public ExporterContainerMenu(final int syncId,
                                 final Player player,
                                 final ExporterBlockEntity exporter,
                                 final ResourceFilterContainer resourceFilterContainer,
                                 final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            player,
            resourceFilterContainer,
            upgradeContainer,
            exporter
        );
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(
            PropertyTypes.EXPORTER_SCHEDULING_MODE,
            ExporterSchedulingModeSettings.FIRST_AVAILABLE
        ));
    }

    @Override
    protected void registerServerProperties(final ExporterBlockEntity blockEntity) {
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
            PropertyTypes.EXPORTER_SCHEDULING_MODE,
            blockEntity::getSchedulingMode,
            blockEntity::setSchedulingMode
        ));
    }
}
