package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.UpgradeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.DestructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DestructorContainerMenu extends AbstractSimpleFilterContainerMenu<DestructorBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "destructor.filter_help");

    public DestructorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(
            Menus.INSTANCE.getDestructor(),
            syncId,
            playerInventory.player,
            buf,
            UpgradeDestinations.DESTRUCTOR,
            FILTER_HELP
        );
    }

    public DestructorContainerMenu(final int syncId,
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
        registerProperty(new ClientProperty<>(PropertyTypes.DESTRUCTOR_PICKUP_ITEMS, false));
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
            PropertyTypes.DESTRUCTOR_PICKUP_ITEMS,
            blockEntity::isPickupItems,
            blockEntity::setPickupItems
        ));
    }
}
