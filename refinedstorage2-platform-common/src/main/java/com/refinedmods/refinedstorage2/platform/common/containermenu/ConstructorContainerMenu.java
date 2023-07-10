package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.block.entity.SchedulingModeType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.constructor.ConstructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ConstructorContainerMenu extends AbstractSimpleFilterContainerMenu<ConstructorBlockEntity> {
    public ConstructorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            playerInventory.player,
            buf,
            UpgradeDestinations.CONSTRUCTOR
        );
    }

    public ConstructorContainerMenu(final int syncId,
                                    final Player player,
                                    final ConstructorBlockEntity constructor,
                                    final ResourceFilterContainer resourceFilterContainer,
                                    final UpgradeContainer upgradeContainer) {
        super(Menus.INSTANCE.getConstructor(), syncId, player, resourceFilterContainer, upgradeContainer, constructor);
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.SCHEDULING_MODE, SchedulingModeType.DEFAULT));
        registerProperty(new ClientProperty<>(PropertyTypes.CONSTRUCTOR_DROP_ITEMS, false));
    }

    @Override
    protected void registerServerProperties(final ConstructorBlockEntity blockEntity) {
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
        registerProperty(new ServerProperty<>(
            PropertyTypes.CONSTRUCTOR_DROP_ITEMS,
            blockEntity::isDropItems,
            blockEntity::setDropItems
        ));
    }
}
