package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterSchedulingModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ExporterContainerMenu extends AbstractResourceFilterContainerMenu implements ResourceTypeAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    public ExporterContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getExporter(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());

        addSlots(
            playerInventory.player,
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9),
            new UpgradeContainer(UpgradeDestinations.EXPORTER, PlatformApi.INSTANCE.getUpgradeRegistry())
        );

        initializeResourceFilterSlots(buf);

        registerProperty(new ClientProperty<>(PropertyTypes.EXACT_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(
            PropertyTypes.EXPORTER_SCHEDULING_MODE,
            ExporterSchedulingModeSettings.FIRST_AVAILABLE
        ));
    }

    public ExporterContainerMenu(final int syncId,
                                 final Player player,
                                 final ExporterBlockEntity exporter,
                                 final ResourceFilterContainer resourceFilterContainer,
                                 final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            resourceFilterContainer
        );
        addSlots(player, resourceFilterContainer, upgradeContainer);

        registerProperty(new ServerProperty<>(
            PropertyTypes.EXACT_MODE,
            exporter::isExactMode,
            exporter::setExactMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            exporter::getRedstoneMode,
            exporter::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.EXPORTER_SCHEDULING_MODE,
            exporter::getSchedulingMode,
            exporter::setSchedulingMode
        ));
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer resourceFilterContainer,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < resourceFilterContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            addSlot(new Slot(upgradeContainer, i, 187, 6 + (i * 18)));
        }
        addPlayerInventory(player.getInventory(), 8, 55);

        transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, FILTER_SLOT_Y);
    }
}
