package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
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

public class ImporterContainerMenu extends AbstractResourceFilterContainerMenu implements ResourceTypeAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    public ImporterContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getImporter(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());

        addSlots(
            playerInventory.player,
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9),
            new UpgradeContainer(UpgradeDestinations.IMPORTER, PlatformApi.INSTANCE.getUpgradeRegistry())
        );

        initializeResourceFilterSlots(buf);

        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(PropertyTypes.EXACT_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    public ImporterContainerMenu(final int syncId,
                                 final Player player,
                                 final ImporterBlockEntity importer,
                                 final ResourceFilterContainer resourceFilterContainer,
                                 final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getImporter(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            resourceFilterContainer
        );
        addSlots(player, resourceFilterContainer, upgradeContainer);

        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            importer::getFilterMode,
            importer::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.EXACT_MODE,
            importer::isExactMode,
            importer::setExactMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            importer::getRedstoneMode,
            importer::setRedstoneMode
        ));
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer resourceFilterContainer,
                          final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        for (int i = 0; i < 4; ++i) {
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
