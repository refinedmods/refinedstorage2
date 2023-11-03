package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractSchedulingNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractSchedulingContainerMenu<T extends AbstractSchedulingNetworkNodeContainerBlockEntity<?, ?>>
    extends AbstractSimpleFilterContainerMenu<T> {
    protected AbstractSchedulingContainerMenu(final MenuType<?> type,
                                              final int syncId,
                                              final Player player,
                                              final ResourceContainer resourceContainer,
                                              final UpgradeContainer upgradeContainer,
                                              final T blockEntity,
                                              final Component filterHelp) {
        super(type, syncId, player, resourceContainer, upgradeContainer, blockEntity, filterHelp);
    }

    protected AbstractSchedulingContainerMenu(final MenuType<?> type,
                                              final int syncId,
                                              final Player player,
                                              final FriendlyByteBuf buf,
                                              final UpgradeDestinations upgradeDestination,
                                              final Component filterHelp) {
        super(type, syncId, player, buf, upgradeDestination, filterHelp);
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.SCHEDULING_MODE, SchedulingModeType.DEFAULT));
    }

    @Override
    protected void registerServerProperties(final T blockEntity) {
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
            PropertyTypes.SCHEDULING_MODE,
            blockEntity::getSchedulingModeType,
            blockEntity::setSchedulingModeType
        ));
    }
}
