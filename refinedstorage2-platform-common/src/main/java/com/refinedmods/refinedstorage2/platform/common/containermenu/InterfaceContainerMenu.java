package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FilteredResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class InterfaceContainerMenu extends AbstractResourceContainerMenu {
    private static final int EXPORT_CONFIG_SLOT_X = 8;
    private static final int EXPORT_CONFIG_SLOT_Y = 20;
    private static final int EXPORT_OUTPUT_SLOT_Y = 66;

    public InterfaceContainerMenu(final int syncId,
                                  final Player player,
                                  final InterfaceBlockEntity blockEntity,
                                  final ResourceContainer exportConfig,
                                  final ResourceContainer exportedItems) {
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
            new FilteredResourceContainer<>(
                9,
                StorageChannelTypes.ITEM,
                ResourceContainerType.FILTER_WITH_AMOUNT,
                ResourceInstance::getInterfaceExportLimit
            ),
            new ResourceContainer(9, ResourceContainerType.CONTAINER)
        );
        initializeResourceSlots(buf);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    private void addSlots(final Player player,
                          final ResourceContainer exportConfig,
                          final ResourceContainer exportedItems) {
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

    private Slot createExportConfigSlot(final ResourceContainer resourceContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceSlot(
            resourceContainer,
            index,
            createTranslation("gui", "interface.filter_help"),
            x,
            EXPORT_CONFIG_SLOT_Y
        );
    }

    private Slot createExportedItemSlot(final ResourceContainer resourceContainer, final int index) {
        final int x = getExportSlotX(index);
        return new ResourceSlot(resourceContainer, index, Component.empty(), x, EXPORT_OUTPUT_SLOT_Y);
    }

    private static int getExportSlotX(final int index) {
        return EXPORT_CONFIG_SLOT_X + (18 * index);
    }
}
