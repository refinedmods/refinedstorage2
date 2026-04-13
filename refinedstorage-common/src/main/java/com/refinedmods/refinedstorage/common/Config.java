package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerSearchMode;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewStyle;
import com.refinedmods.refinedstorage.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.grid.GridViewType;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSize;

import java.util.Optional;

import net.minecraft.resources.Identifier;

public interface Config {
    ScreenSize getScreenSize();

    void setScreenSize(ScreenSize size);

    boolean isDebug();

    boolean isSmoothScrolling();

    boolean isSearchBoxAutoSelected();

    void setSearchBoxAutoSelected(boolean searchBoxAutoSelected);

    int getMaxRowsStretch();

    boolean isAutocraftingNotification();

    void setAutocraftingNotification(boolean autocraftingNotification);

    AutocraftingPreviewStyle getAutocraftingPreviewStyle();

    void setAutocraftingPreviewStyle(AutocraftingPreviewStyle autocraftingPreviewStyle);

    GridEntry getGrid();

    SimpleEnergyUsageEntry getPatternGrid();

    CraftingGridEntry getCraftingGrid();

    ControllerEntry getController();

    DiskDriveEntry getDiskDrive();

    DiskInterfaceEntry getDiskInterface();

    SimpleEnergyUsageEntry getCable();

    StorageBlockEntry getStorageBlock();

    FluidStorageBlockEntry getFluidStorageBlock();

    SimpleEnergyUsageEntry getImporter();

    SimpleEnergyUsageEntry getExporter();

    UpgradeEntry getUpgrade();

    SimpleEnergyUsageEntry getInterface();

    SimpleEnergyUsageEntry getExternalStorage();

    SimpleEnergyUsageEntry getDetector();

    SimpleEnergyUsageEntry getDestructor();

    SimpleEnergyUsageEntry getConstructor();

    WirelessGridEntry getWirelessGrid();

    WirelessTransmitterEntry getWirelessTransmitter();

    SimpleEnergyUsageEntry getStorageMonitor();

    SimpleEnergyUsageEntry getNetworkReceiver();

    SimpleEnergyUsageEntry getNetworkTransmitter();

    PortableGridEntry getPortableGrid();

    SimpleEnergyUsageEntry getSecurityCard();

    SimpleEnergyUsageEntry getFallbackSecurityCard();

    SimpleEnergyUsageEntry getSecurityManager();

    RelayEntry getRelay();

    AutocrafterEntry getAutocrafter();

    AutocrafterManagerEntry getAutocrafterManager();

    SimpleEnergyUsageEntry getAutocraftingMonitor();

    WirelessAutocraftingMonitorEntry getWirelessAutocraftingMonitor();

    interface SimpleEnergyUsageEntry {
        long getEnergyUsage();
    }

    interface GridEntry extends SimpleEnergyUsageEntry {
        boolean isLargeFont();

        boolean isPreventSortingWhileShiftIsDown();

        boolean isDetailedTooltip();

        boolean isRememberSearchQuery();

        Optional<Identifier> getSynchronizer();

        void setSynchronizer(Identifier synchronizerId);

        void clearSynchronizer();

        SortingDirection getSortingDirection();

        void setSortingDirection(SortingDirection sortingDirection);

        GridSortingTypes getSortingType();

        void setSortingType(GridSortingTypes sortingType);

        GridViewType getViewType();

        void setViewType(GridViewType viewType);

        Optional<Identifier> getResourceType();

        void setResourceType(Identifier resourceTypeId);

        void clearResourceType();
    }

    interface CraftingGridEntry extends SimpleEnergyUsageEntry {
        CraftingGridMatrixCloseBehavior getCraftingMatrixCloseBehavior();
    }

    interface DiskDriveEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerDisk();
    }

    interface DiskInterfaceEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerDisk();
    }

    interface ControllerEntry {
        long getEnergyCapacity();
    }

    interface StorageBlockEntry {
        long get1kEnergyUsage();

        long get4kEnergyUsage();

        long get16kEnergyUsage();

        long get64kEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface FluidStorageBlockEntry {
        long get64bEnergyUsage();

        long get256bEnergyUsage();

        long get1024bEnergyUsage();

        long get4096bEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface UpgradeEntry {
        long getSpeedUpgradeEnergyUsage();

        long getStackUpgradeEnergyUsage();

        long getFortune1UpgradeEnergyUsage();

        long getFortune2UpgradeEnergyUsage();

        long getFortune3UpgradeEnergyUsage();

        long getSilkTouchUpgradeEnergyUsage();

        long getRegulatorUpgradeEnergyUsage();

        long getRangeUpgradeEnergyUsage();

        long getCreativeRangeUpgradeEnergyUsage();

        int getRangeUpgradeRange();

        long getAutocraftingUpgradeEnergyUsage();
    }

    interface WirelessGridEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getInsertEnergyUsage();

        long getExtractEnergyUsage();
    }

    interface WirelessTransmitterEntry extends SimpleEnergyUsageEntry {
        int getBaseRange();
    }

    interface PortableGridEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getInsertEnergyUsage();

        long getExtractEnergyUsage();
    }

    interface RelayEntry {
        long getInputNetworkEnergyUsage();

        long getOutputNetworkEnergyUsage();
    }

    interface AutocrafterEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerPattern();
    }

    interface AutocrafterManagerEntry extends SimpleEnergyUsageEntry {
        void setSearchMode(AutocrafterManagerSearchMode searchMode);

        AutocrafterManagerSearchMode getSearchMode();

        void setViewType(AutocrafterManagerViewType viewType);

        AutocrafterManagerViewType getViewType();
    }

    interface WirelessAutocraftingMonitorEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getCancelEnergyUsage();

        long getCancelAllEnergyUsage();
    }
}
