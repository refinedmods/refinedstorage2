package com.refinedmods.refinedstorage.platform.common.exporter;

import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractSchedulingContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterContainerMenu extends AbstractSchedulingContainerMenu<ExporterBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "exporter.filter_help");

    public ExporterContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.EXPORTER,
            FILTER_HELP
        );
    }

    ExporterContainerMenu(final int syncId,
                          final Player player,
                          final ExporterBlockEntity exporter,
                          final ResourceContainer resourceContainer,
                          final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            exporter,
            FILTER_HELP
        );
    }
}
