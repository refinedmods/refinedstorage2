package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractSchedulingContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ConstructorContainerMenu extends AbstractSchedulingContainerMenu<ConstructorBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "constructor.filter_help");

    public ConstructorContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.CONSTRUCTOR,
            FILTER_HELP
        );
    }

    ConstructorContainerMenu(final int syncId,
                             final Player player,
                             final ConstructorBlockEntity constructor,
                             final ResourceContainer resourceContainer,
                             final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            constructor,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        super.registerClientProperties();
        registerProperty(new ClientProperty<>(ConstructorDestructorPropertyTypes.DROP_ITEMS, false));
    }

    @Override
    protected void registerServerProperties(final ConstructorBlockEntity blockEntity) {
        super.registerServerProperties(blockEntity);
        registerProperty(new ServerProperty<>(
            ConstructorDestructorPropertyTypes.DROP_ITEMS,
            blockEntity::isDropItems,
            blockEntity::setDropItems
        ));
    }
}
