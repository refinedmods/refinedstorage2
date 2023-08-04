package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.constructor.ConstructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ConstructorContainerMenu extends AbstractSchedulingContainerMenu<ConstructorBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "constructor.filter_help");

    public ConstructorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            playerInventory.player,
            buf,
            UpgradeDestinations.CONSTRUCTOR,
            FILTER_HELP
        );
    }

    public ConstructorContainerMenu(final int syncId,
                                    final Player player,
                                    final ConstructorBlockEntity constructor,
                                    final ResourceFilterContainer resourceFilterContainer,
                                    final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            player,
            resourceFilterContainer,
            upgradeContainer,
            constructor,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        super.registerClientProperties();
        registerProperty(new ClientProperty<>(PropertyTypes.CONSTRUCTOR_DROP_ITEMS, false));
    }

    @Override
    protected void registerServerProperties(final ConstructorBlockEntity blockEntity) {
        super.registerServerProperties(blockEntity);
        registerProperty(new ServerProperty<>(
            PropertyTypes.CONSTRUCTOR_DROP_ITEMS,
            blockEntity::isDropItems,
            blockEntity::setDropItems
        ));
    }
}
