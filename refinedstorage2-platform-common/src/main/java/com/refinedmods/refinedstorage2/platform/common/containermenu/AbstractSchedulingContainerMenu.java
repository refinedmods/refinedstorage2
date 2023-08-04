package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractSchedulingNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.SchedulingModeType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractSchedulingContainerMenu<T extends AbstractSchedulingNetworkNodeContainerBlockEntity<?, ?>>
    extends AbstractSimpleFilterContainerMenu<T> {
    protected AbstractSchedulingContainerMenu(final MenuType<?> type,
                                              final int syncId,
                                              final Player player,
                                              final ResourceFilterContainer resourceFilterContainer,
                                              final UpgradeContainer upgradeContainer,
                                              final T blockEntity,
                                              final Component filterHelp) {
        super(type, syncId, player, resourceFilterContainer, upgradeContainer, blockEntity, filterHelp);
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
