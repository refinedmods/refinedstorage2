package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.storage.StoragePropertyTypes;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractSimpleFilterContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;

import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class RelayContainerMenu extends AbstractSimpleFilterContainerMenu<RelayBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "relay.filter_help");

    private final Predicate<Player> stillValid;

    @Nullable
    private PassThroughListener passThroughListener;

    public RelayContainerMenu(final int syncId,
                              final Inventory playerInventory,
                              final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getRelay(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            null,
            FILTER_HELP
        );
        this.stillValid = p -> true;
    }

    RelayContainerMenu(final int syncId,
                       final Player player,
                       final RelayBlockEntity relay,
                       final ResourceContainer resourceContainer) {
        super(
            Menus.INSTANCE.getRelay(),
            syncId,
            player,
            resourceContainer,
            null,
            relay,
            FILTER_HELP
        );
        this.stillValid = p -> Container.stillValidBlockEntity(relay, p);
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(RelayPropertyTypes.PASS_THROUGH, true) {
            @Override
            protected void onChangedOnClient(final Boolean newValue) {
                super.onChangedOnClient(newValue);
                if (passThroughListener != null) {
                    passThroughListener.passThroughChanged(
                        Boolean.TRUE.equals(newValue),
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_STORAGE).getValue()),
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_AUTOCRAFTING).getValue())
                    );
                }
            }
        });
        registerProperty(new ClientProperty<>(RelayPropertyTypes.PASS_ENERGY, false));
        registerProperty(new ClientProperty<>(RelayPropertyTypes.PASS_STORAGE, false) {
            @Override
            protected void onChangedOnClient(final Boolean newValue) {
                super.onChangedOnClient(newValue);
                if (passThroughListener != null) {
                    passThroughListener.passThroughChanged(
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_THROUGH).getValue()),
                        Boolean.TRUE.equals(newValue),
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_AUTOCRAFTING).getValue())
                    );
                }
            }
        });
        registerProperty(new ClientProperty<>(RelayPropertyTypes.PASS_SECURITY, false));
        registerProperty(new ClientProperty<>(RelayPropertyTypes.PASS_AUTOCRAFTING, false) {
            @Override
            protected void onChangedOnClient(final Boolean newValue) {
                super.onChangedOnClient(newValue);
                if (passThroughListener != null) {
                    passThroughListener.passThroughChanged(
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_THROUGH).getValue()),
                        Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_STORAGE).getValue()),
                        Boolean.TRUE.equals(newValue)
                    );
                }
            }
        });
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.ACCESS_MODE, AccessMode.INSERT_EXTRACT));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.INSERT_PRIORITY, 0));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.EXTRACT_PRIORITY, 0));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
    }

    @Override
    protected void registerServerProperties(final RelayBlockEntity blockEntity) {
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            RelayPropertyTypes.PASS_THROUGH,
            blockEntity::isPassThrough,
            blockEntity::setPassThrough
        ));
        registerProperty(new ServerProperty<>(
            RelayPropertyTypes.PASS_ENERGY,
            blockEntity::isPassEnergy,
            blockEntity::setPassEnergy
        ));
        registerProperty(new ServerProperty<>(
            RelayPropertyTypes.PASS_STORAGE,
            blockEntity::isPassStorage,
            blockEntity::setPassStorage
        ));
        registerProperty(new ServerProperty<>(
            RelayPropertyTypes.PASS_SECURITY,
            blockEntity::isPassSecurity,
            blockEntity::setPassSecurity
        ));
        registerProperty(new ServerProperty<>(
            RelayPropertyTypes.PASS_AUTOCRAFTING,
            blockEntity::isPassAutocrafting,
            blockEntity::setPassAutocrafting
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            blockEntity::getFilterMode,
            blockEntity::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.ACCESS_MODE,
            blockEntity::getAccessMode,
            blockEntity::setAccessMode
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.INSERT_PRIORITY,
            blockEntity::getInsertPriority,
            blockEntity::setInsertPriority
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.EXTRACT_PRIORITY,
            blockEntity::getExtractPriority,
            blockEntity::setExtractPriority
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            blockEntity::isFuzzyMode,
            blockEntity::setFuzzyMode
        ));
    }

    boolean isPassThrough() {
        return Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_THROUGH).getValue());
    }

    boolean isPassStorage() {
        return Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_STORAGE).getValue());
    }

    boolean isPassAutocrafting() {
        return Boolean.TRUE.equals(getProperty(RelayPropertyTypes.PASS_AUTOCRAFTING).getValue());
    }

    void setPassThroughListener(final PassThroughListener passThroughListener) {
        this.passThroughListener = passThroughListener;
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }

    interface PassThroughListener {
        void passThroughChanged(boolean passThrough, boolean passStorage, boolean passAutocrafting);
    }
}
