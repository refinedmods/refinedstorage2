package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterContainerMenu extends AbstractSchedulingContainerMenu<ExporterBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "exporter.filter_help");

    public ExporterContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            playerInventory.player,
            buf,
            UpgradeDestinations.EXPORTER,
            FILTER_HELP
        );
    }

    public ExporterContainerMenu(final int syncId,
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
