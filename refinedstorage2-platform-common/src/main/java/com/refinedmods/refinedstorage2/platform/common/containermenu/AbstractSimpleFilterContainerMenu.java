package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractSimpleFilterContainerMenu<T extends BlockEntity>
    extends AbstractResourceFilterContainerMenu
    implements ResourceTypeAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    protected AbstractSimpleFilterContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final OrderedRegistry<ResourceLocation, ResourceType> rtr,
                                                final Player player,
                                                final ResourceFilterContainer resourceFilterContainer,
                                                final UpgradeContainer upgradeContainer,
                                                final T blockEntity) {
        super(type, syncId, rtr, player, resourceFilterContainer);
        registerServerProperties(blockEntity);
        addSlots(player, resourceFilterContainer, upgradeContainer);
    }

    protected AbstractSimpleFilterContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final OrderedRegistry<ResourceLocation, ResourceType> rtr,
                                                final Player player,
                                                final FriendlyByteBuf buf,
                                                final UpgradeDestinations upgradeDestination) {
        super(type, syncId, rtr);
        registerClientProperties();
        addSlots(
            player,
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, false),
            new UpgradeContainer(upgradeDestination, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        initializeResourceFilterSlots(buf);
    }

    protected abstract void registerClientProperties();

    protected abstract void registerServerProperties(T blockEntity);

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
