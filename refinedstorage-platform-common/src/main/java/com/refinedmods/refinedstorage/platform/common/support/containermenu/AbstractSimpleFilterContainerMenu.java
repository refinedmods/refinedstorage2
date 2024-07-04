package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeSlot;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractSimpleFilterContainerMenu<T extends BlockEntity>
    extends AbstractResourceContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final Component filterHelp;

    protected AbstractSimpleFilterContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                @Nullable final UpgradeContainer upgradeContainer,
                                                final T blockEntity,
                                                final Component filterHelp) {
        super(type, syncId, player);
        this.filterHelp = filterHelp;
        registerServerProperties(blockEntity);
        addSlots(player, resourceContainer, upgradeContainer);
    }

    protected AbstractSimpleFilterContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainerData resourceContainerData,
                                                @Nullable final UpgradeDestinations upgradeDestination,
                                                final Component filterHelp) {
        super(type, syncId);
        this.filterHelp = filterHelp;
        registerClientProperties();
        addSlots(
            player,
            ResourceContainerImpl.createForFilter(resourceContainerData),
            upgradeDestination == null
                ? null
                : new UpgradeContainer(upgradeDestination, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
    }

    protected abstract void registerClientProperties();

    protected abstract void registerServerProperties(T blockEntity);

    private void addSlots(final Player player,
                          final ResourceContainer resourceContainer,
                          @Nullable final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < resourceContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceContainer, i));
        }
        if (upgradeContainer != null) {
            for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
                addSlot(new UpgradeSlot(upgradeContainer, i, 187, 6 + (i * 18)));
            }
        }
        addPlayerInventory(player.getInventory(), 8, 55);

        if (upgradeContainer != null) {
            transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
        }
        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer resourceContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(resourceContainer, i, filterHelp, x, FILTER_SLOT_Y, ResourceSlotType.FILTER);
    }
}
