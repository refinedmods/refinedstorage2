package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractSimpleFilterContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class DestructorContainerMenu extends AbstractSimpleFilterContainerMenu<DestructorBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "destructor.filter_help");

    public DestructorContainerMenu(final int syncId,
                                   final Inventory playerInventory,
                                   final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getDestructor(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.DESTRUCTOR,
            FILTER_HELP
        );
    }

    DestructorContainerMenu(final int syncId,
                            final Player player,
                            final DestructorBlockEntity destructor,
                            final ResourceContainer resourceContainer,
                            final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getDestructor(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            destructor,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(ConstructorDestructorPropertyTypes.PICKUP_ITEMS, false));
    }

    @Override
    protected void registerServerProperties(final DestructorBlockEntity blockEntity) {
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            blockEntity::getFilterMode,
            blockEntity::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            ConstructorDestructorPropertyTypes.PICKUP_ITEMS,
            blockEntity::isPickupItems,
            blockEntity::setPickupItems
        ));
    }
}
