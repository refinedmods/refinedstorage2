package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractStorageContainerMenu extends AbstractResourceFilterContainerMenu {
    protected AbstractStorageContainerMenu(final MenuType<?> type,
                                           final int syncId,
                                           final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry) {
        super(type, syncId, resourceTypeRegistry);

        registerProperty(new ClientProperty<>(PropertyTypes.PRIORITY, 0));
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.ACCESS_MODE, AccessMode.INSERT_EXTRACT));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    protected AbstractStorageContainerMenu(final MenuType<?> type,
                                           final int syncId,
                                           final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry,
                                           final Player player,
                                           final StorageConfigurationProvider storageConfigurationProvider,
                                           final ResourceFilterContainer container) {
        super(type, syncId, resourceTypeRegistry, player, container);

        registerProperty(new ServerProperty<>(
            PropertyTypes.PRIORITY,
            storageConfigurationProvider::getPriority,
            storageConfigurationProvider::setPriority
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            storageConfigurationProvider::getFilterMode,
            storageConfigurationProvider::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            storageConfigurationProvider::isFuzzyMode,
            storageConfigurationProvider::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.ACCESS_MODE,
            storageConfigurationProvider::getAccessMode,
            storageConfigurationProvider::setAccessMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            storageConfigurationProvider::getRedstoneMode,
            storageConfigurationProvider::setRedstoneMode
        ));
    }
}
