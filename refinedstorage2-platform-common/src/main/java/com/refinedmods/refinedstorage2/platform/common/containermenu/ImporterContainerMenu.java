package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ImporterContainerMenu extends AbstractResourceFilterContainerMenu
    implements FilterModeAccessor, ExactModeAccessor, RedstoneModeAccessor, ResourceTypeAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final TwoWaySyncProperty<FilterMode> filterModeProperty;
    private final TwoWaySyncProperty<Boolean> exactModeProperty;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;

    public ImporterContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getImporter(), syncId, PlatformApi.INSTANCE.getResourceTypeRegistry());

        addSlots(
            playerInventory.player,
            new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, () -> {
            })
        );

        initializeResourceFilterSlots(buf);

        this.filterModeProperty = FilterModeSettings.createClientSyncProperty(0);
        this.exactModeProperty = TwoWaySyncProperty.booleanForClient(1);
        this.redstoneModeProperty = RedstoneModeSettings.createClientSyncProperty(2);

        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(redstoneModeProperty);
    }

    public ImporterContainerMenu(final int syncId,
                                 final Player player,
                                 final ImporterBlockEntity importer,
                                 final ResourceFilterContainer resourceFilterContainer) {
        super(
            Menus.INSTANCE.getImporter(),
            syncId,
            PlatformApi.INSTANCE.getResourceTypeRegistry(),
            player,
            resourceFilterContainer
        );
        addSlots(player, resourceFilterContainer);

        this.filterModeProperty = TwoWaySyncProperty.forServer(
            0,
            FilterModeSettings::getFilterMode,
            FilterModeSettings::getFilterMode,
            importer::getFilterMode,
            importer::setFilterMode
        );
        this.exactModeProperty = TwoWaySyncProperty.forServer(
            1,
            value -> Boolean.TRUE.equals(value) ? 0 : 1,
            value -> value == 0,
            importer::isExactMode,
            importer::setExactMode
        );
        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
            2,
            RedstoneModeSettings::getRedstoneMode,
            RedstoneModeSettings::getRedstoneMode,
            importer::getRedstoneMode,
            importer::setRedstoneMode
        );

        addDataSlot(filterModeProperty);
        addDataSlot(exactModeProperty);
        addDataSlot(redstoneModeProperty);
    }

    private void addSlots(final Player player,
                          final ResourceFilterContainer resourceFilterContainer) {
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(resourceFilterContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 55);
    }

    private Slot createFilterSlot(final ResourceFilterContainer resourceFilterContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceFilterSlot(resourceFilterContainer, i, x, FILTER_SLOT_Y);
    }

    @Override
    public FilterMode getFilterMode() {
        return filterModeProperty.getDeserialized();
    }

    @Override
    public void setFilterMode(final FilterMode filterMode) {
        filterModeProperty.syncToServer(filterMode);
    }

    @Override
    public boolean isExactMode() {
        return exactModeProperty.getDeserialized();
    }

    @Override
    public void setExactMode(final boolean exactMode) {
        exactModeProperty.syncToServer(exactMode);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeProperty.getDeserialized();
    }

    @Override
    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }
}
