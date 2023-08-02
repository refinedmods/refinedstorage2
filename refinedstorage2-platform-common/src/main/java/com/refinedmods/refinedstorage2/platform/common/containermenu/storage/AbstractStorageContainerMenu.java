package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractStorageContainerMenu extends AbstractResourceFilterContainerMenu {
    protected AbstractStorageContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
        registerProperty(new ClientProperty<>(PropertyTypes.PRIORITY, 0));
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.ACCESS_MODE, AccessMode.INSERT_EXTRACT));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    protected AbstractStorageContainerMenu(final MenuType<?> type,
                                           final int syncId,
                                           final Player player,
                                           final StorageConfigurationContainer configContainer) {
        super(type, syncId, player);
        registerProperty(new ServerProperty<>(
            PropertyTypes.PRIORITY,
            configContainer::getPriority,
            configContainer::setPriority
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            configContainer::getFilterMode,
            configContainer::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            configContainer::isFuzzyMode,
            configContainer::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.ACCESS_MODE,
            configContainer::getAccessMode,
            configContainer::setAccessMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            configContainer::getRedstoneMode,
            configContainer::setRedstoneMode
        ));
    }

    public boolean shouldDisplayFilterModeWarning() {
        return getProperty(PropertyTypes.FILTER_MODE).getValue() == FilterMode.ALLOW && !isFilterConfigured();
    }
}
