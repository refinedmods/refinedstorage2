package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class InterfaceContainerMenu extends AbstractResourceFilterContainerMenu {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;
    private static final int EXPORT_OUTPUT_SLOT_Y = 66;

    public InterfaceContainerMenu(final int syncId,
                                  final Player player,
                                  final InterfaceBlockEntity blockEntity,
                                  final ResourceFilterContainer exportConfig,
                                  final Container exportedItems) {
        super(
            Menus.INSTANCE.getInterface(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            exportConfig
        );
        addSlots(player, exportConfig, exportedItems);

        registerProperty(new ServerProperty<>(
            PropertyTypes.EXACT_MODE,
            blockEntity::isExactMode,
            blockEntity::setExactMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
    }

    public InterfaceContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getInterface(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());
        addSlots(
            playerInventory.player,
            new FilteredResourceFilterContainer(
                PlatformApi.INSTANCE.getResourceTypeRegistry(),
                9,
                ItemResourceType.INSTANCE,
                64
            ),
            new SimpleContainer(9)
        );
        initializeResourceFilterSlots(buf);

        registerProperty(new ClientProperty<>(PropertyTypes.EXACT_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer exportConfig,
                          final Container exportedItems) {
        for (int i = 0; i < exportConfig.size(); ++i) {
            addSlot(createExportConfigSlot(exportConfig, i));
        }
        for (int i = 0; i < exportedItems.getContainerSize(); ++i) {
            addSlot(createExportedItemSlot(exportedItems, i));
        }

        addPlayerInventory(player.getInventory(), 8, 100);

        transferManager.addBiTransfer(exportedItems, player.getInventory());
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createExportConfigSlot(final ResourceFilterContainer resourceFilterContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceFilterSlot(resourceFilterContainer, index, x, EXPORT_CONFIG_SLOT_Y);
    }

    private Slot createExportedItemSlot(final Container container, final int index) {
        final int x = getExportSlotX(index);
        return new Slot(container, index, x, EXPORT_OUTPUT_SLOT_Y);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}
