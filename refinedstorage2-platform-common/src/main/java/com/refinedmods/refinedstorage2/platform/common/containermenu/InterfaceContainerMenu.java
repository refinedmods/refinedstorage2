package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.FilteredResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainerType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class InterfaceContainerMenu extends AbstractResourceFilterContainerMenu {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;
    private static final int EXPORT_OUTPUT_SLOT_Y = 66;

    public InterfaceContainerMenu(final int syncId,
                                  final Player player,
                                  final InterfaceBlockEntity blockEntity,
                                  final ResourceFilterContainer exportConfig,
                                  final ResourceFilterContainer exportedItems) {
        super(Menus.INSTANCE.getInterface(), syncId, player);
        addSlots(player, exportConfig, exportedItems);
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
    }

    public InterfaceContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getInterface(), syncId);
        addSlots(
            playerInventory.player,
            new FilteredResourceFilterContainer<>(9, StorageChannelTypes.ITEM, 64),
            new ResourceFilterContainer(9, ResourceFilterContainerType.CONTAINER)
        );
        initializeResourceFilterSlots(buf);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer exportConfig,
                          final ResourceFilterContainer exportedItems) {
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
        return new ResourceFilterSlot(
            resourceFilterContainer,
            index,
            createTranslation("gui", "interface.filter_help"),
            x,
            EXPORT_CONFIG_SLOT_Y
        );
    }

    private Slot createExportedItemSlot(final ResourceFilterContainer resourceFilterContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceFilterSlot(resourceFilterContainer, index, Component.empty(), x, EXPORT_OUTPUT_SLOT_Y);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}
