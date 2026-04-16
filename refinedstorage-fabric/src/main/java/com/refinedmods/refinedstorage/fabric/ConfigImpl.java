package com.refinedmods.refinedstorage.fabric;

import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerSearchMode;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewStyle;
import com.refinedmods.refinedstorage.common.content.DefaultEnergyUsage;
import com.refinedmods.refinedstorage.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.grid.GridViewType;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSize;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import java.util.Optional;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
@Config(name = IdentifierUtil.MOD_ID)
public class ConfigImpl implements ConfigData, com.refinedmods.refinedstorage.common.Config {
    private ScreenSize screenSize = ScreenSize.STRETCH;

    private boolean requireEnergy = true;

    private boolean smoothScrolling = true;

    private boolean tenthAnniversaryCape = false;

    private boolean autocraftingNotification = true;

    private AutocraftingPreviewStyle autocraftingPreviewStyle = AutocraftingPreviewStyle.LIST;

    private boolean searchBoxAutoSelected = false;

    @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
    private int maxRowsStretch = 256;

    @ConfigEntry.Gui.CollapsibleObject
    private GridEntryImpl grid = new GridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl patternGrid = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.PATTERN_GRID);

    @ConfigEntry.Gui.CollapsibleObject
    private CraftingGridEntryImpl craftingGrid = new CraftingGridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ControllerEntryImpl controller = new ControllerEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskDriveEntryImpl diskDrive = new DiskDriveEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskInterfaceEntryImpl diskInterface = new DiskInterfaceEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl cable = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.CABLE);

    @ConfigEntry.Gui.CollapsibleObject
    private StorageBlockEntryImpl storageBlock = new StorageBlockEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private FluidStorageBlockEntryImpl fluidStorageBlock = new FluidStorageBlockEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl importer = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.IMPORTER);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl exporter = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.EXPORTER);

    @ConfigEntry.Gui.CollapsibleObject
    private UpgradeEntryImpl upgrade = new UpgradeEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl iface = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.INTERFACE);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl externalStorage = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.EXTERNAL_STORAGE
    );

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl detector = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.DETECTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl destructor = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.DESTRUCTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl constructor = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.CONSTRUCTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private WirelessGridEntryImpl wirelessGrid = new WirelessGridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private WirelessTransmitterEntryImpl wirelessTransmitter = new WirelessTransmitterEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl storageMonitor = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.STORAGE_MONITOR
    );

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl networkReceiver = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.NETWORK_RECEIVER
    );

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl networkTransmitter = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.NETWORK_TRANSMITTER
    );

    @ConfigEntry.Gui.CollapsibleObject
    private PortableGridEntryImpl portableGrid = new PortableGridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl securityCard = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.SECURITY_CARD);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl fallbackSecurityCard = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.FALLBACK_SECURITY_CARD
    );

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl securityManager = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.SECURITY_MANAGER
    );

    @ConfigEntry.Gui.CollapsibleObject
    private RelayEntryImpl relay = new RelayEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private AutocrafterEntryImpl autocrafter = new AutocrafterEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private AutocrafterManagerEntryImpl autocrafterManager = new AutocrafterManagerEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl autocraftingMonitor = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.AUTOCRAFTING_MONITOR
    );

    @ConfigEntry.Gui.CollapsibleObject
    private WirelessAutocraftingMonitorEntryImpl wirelessAutocraftingMonitor =
        new WirelessAutocraftingMonitorEntryImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public ScreenSize getScreenSize() {
        return screenSize;
    }

    @Override
    public void setScreenSize(final ScreenSize screenSize) {
        this.screenSize = screenSize;
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }

    @Override
    public boolean isRequireEnergy() {
        return requireEnergy;
    }

    @Override
    public boolean isTenthAnniversaryCape() {
        return tenthAnniversaryCape;
    }

    @Override
    public void setTenthAnniversaryCape(final boolean enabled) {
        this.tenthAnniversaryCape = enabled;
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }

    @Override
    public boolean isSearchBoxAutoSelected() {
        return searchBoxAutoSelected;
    }

    @Override
    public void setSearchBoxAutoSelected(final boolean searchBoxAutoSelected) {
        this.searchBoxAutoSelected = searchBoxAutoSelected;
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }

    @Override
    public int getMaxRowsStretch() {
        return maxRowsStretch;
    }

    @Override
    public boolean isAutocraftingNotification() {
        return autocraftingNotification;
    }

    @Override
    public void setAutocraftingNotification(final boolean autocraftingNotification) {
        this.autocraftingNotification = autocraftingNotification;
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }

    @Override
    public AutocraftingPreviewStyle getAutocraftingPreviewStyle() {
        return autocraftingPreviewStyle;
    }

    @Override
    public void setAutocraftingPreviewStyle(final AutocraftingPreviewStyle autocraftingPreviewStyle) {
        this.autocraftingPreviewStyle = autocraftingPreviewStyle;
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }

    @Override
    public GridEntry getGrid() {
        return grid;
    }

    @Override
    public SimpleEnergyUsageEntry getPatternGrid() {
        return patternGrid;
    }

    @Override
    public CraftingGridEntry getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public ControllerEntry getController() {
        return controller;
    }

    @Override
    public DiskDriveEntry getDiskDrive() {
        return diskDrive;
    }

    @Override
    public DiskInterfaceEntry getDiskInterface() {
        return diskInterface;
    }

    @Override
    public SimpleEnergyUsageEntry getCable() {
        return cable;
    }

    @Override
    public StorageBlockEntry getStorageBlock() {
        return storageBlock;
    }

    @Override
    public FluidStorageBlockEntry getFluidStorageBlock() {
        return fluidStorageBlock;
    }

    @Override
    public SimpleEnergyUsageEntry getImporter() {
        return importer;
    }

    @Override
    public SimpleEnergyUsageEntry getExporter() {
        return exporter;
    }

    @Override
    public UpgradeEntry getUpgrade() {
        return upgrade;
    }

    @Override
    public SimpleEnergyUsageEntry getInterface() {
        return iface;
    }

    @Override
    public SimpleEnergyUsageEntry getExternalStorage() {
        return externalStorage;
    }

    @Override
    public SimpleEnergyUsageEntry getDetector() {
        return detector;
    }

    @Override
    public SimpleEnergyUsageEntry getDestructor() {
        return destructor;
    }

    @Override
    public SimpleEnergyUsageEntry getConstructor() {
        return constructor;
    }

    @Override
    public WirelessGridEntry getWirelessGrid() {
        return wirelessGrid;
    }

    @Override
    public WirelessTransmitterEntry getWirelessTransmitter() {
        return wirelessTransmitter;
    }

    @Override
    public SimpleEnergyUsageEntry getStorageMonitor() {
        return storageMonitor;
    }

    @Override
    public SimpleEnergyUsageEntry getNetworkReceiver() {
        return networkReceiver;
    }

    @Override
    public SimpleEnergyUsageEntry getNetworkTransmitter() {
        return networkTransmitter;
    }

    @Override
    public PortableGridEntry getPortableGrid() {
        return portableGrid;
    }

    @Override
    public SimpleEnergyUsageEntry getSecurityCard() {
        return securityCard;
    }

    @Override
    public SimpleEnergyUsageEntry getFallbackSecurityCard() {
        return fallbackSecurityCard;
    }

    @Override
    public SimpleEnergyUsageEntry getSecurityManager() {
        return securityManager;
    }

    @Override
    public RelayEntry getRelay() {
        return relay;
    }

    @Override
    public AutocrafterEntry getAutocrafter() {
        return autocrafter;
    }

    @Override
    public AutocrafterManagerEntry getAutocrafterManager() {
        return autocrafterManager;
    }

    @Override
    public SimpleEnergyUsageEntry getAutocraftingMonitor() {
        return autocraftingMonitor;
    }

    @Override
    public WirelessAutocraftingMonitorEntry getWirelessAutocraftingMonitor() {
        return wirelessAutocraftingMonitor;
    }

    private static class GridEntryImpl implements GridEntry {
        private boolean largeFont = false;

        private long energyUsage = DefaultEnergyUsage.GRID;

        private boolean preventSortingWhileShiftIsDown = true;

        private boolean detailedTooltip = true;

        private boolean rememberSearchQuery = false;

        private String synchronizer = "";

        private String resourceType = "";

        private SortingDirection sortingDirection = SortingDirection.ASCENDING;

        private GridSortingTypes sortingType = GridSortingTypes.QUANTITY;

        private GridViewType viewType = GridViewType.ALL;

        @Override
        public boolean isLargeFont() {
            return largeFont;
        }

        @Override
        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown;
        }

        @Override
        public boolean isDetailedTooltip() {
            return detailedTooltip;
        }

        @Override
        public boolean isRememberSearchQuery() {
            return rememberSearchQuery;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public Optional<ResourceLocation> getSynchronizer() {
            if (synchronizer.trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(synchronizer).map(ResourceLocation::tryParse);
        }

        @Override
        public void setSynchronizer(final ResourceLocation synchronizerId) {
            this.synchronizer = synchronizerId.toString();
            save();
        }

        @Override
        public void clearSynchronizer() {
            this.synchronizer = "";
            save();
        }

        @Override
        public SortingDirection getSortingDirection() {
            return sortingDirection;
        }

        @Override
        public void setSortingDirection(final SortingDirection sortingDirection) {
            this.sortingDirection = sortingDirection;
            save();
        }

        @Override
        public GridSortingTypes getSortingType() {
            return sortingType;
        }

        @Override
        public void setSortingType(final GridSortingTypes sortingType) {
            this.sortingType = sortingType;
            save();
        }

        @Override
        public GridViewType getViewType() {
            return viewType;
        }

        @Override
        public void setViewType(final GridViewType viewType) {
            this.viewType = viewType;
            save();
        }

        @Override
        public Optional<ResourceLocation> getResourceType() {
            if (resourceType.trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(resourceType).map(ResourceLocation::tryParse);
        }

        @Override
        public void setResourceType(final ResourceLocation resourceType) {
            this.resourceType = resourceType.toString();
            save();
        }

        @Override
        public void clearResourceType() {
            this.resourceType = "";
            save();
        }

        private static void save() {
            AutoConfig.getConfigHolder(ConfigImpl.class).save();
        }
    }

    private static class CraftingGridEntryImpl implements CraftingGridEntry {
        private long energyUsage = DefaultEnergyUsage.CRAFTING_GRID;

        private CraftingGridMatrixCloseBehavior craftingMatrixCloseBehavior = CraftingGridMatrixCloseBehavior.NONE;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public CraftingGridMatrixCloseBehavior getCraftingMatrixCloseBehavior() {
            return craftingMatrixCloseBehavior;
        }
    }

    private static class DiskDriveEntryImpl implements DiskDriveEntry {
        private long energyUsage = DefaultEnergyUsage.DISK_DRIVE;

        private long energyUsagePerDisk = DefaultEnergyUsage.DISK_DRIVE_PER_DISK;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    private static class DiskInterfaceEntryImpl implements DiskInterfaceEntry {
        private long energyUsage = DefaultEnergyUsage.DISK_INTERFACE;

        private long energyUsagePerDisk = DefaultEnergyUsage.DISK_INTERFACE_PER_DISK;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    private static class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private long energyUsage;

        SimpleEnergyUsageEntryImpl(final long energyUsage) {
            this.energyUsage = energyUsage;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class ControllerEntryImpl implements ControllerEntry {
        private long energyCapacity = DefaultEnergyUsage.CONTROLLER_CAPACITY;

        public long getEnergyCapacity() {
            return energyCapacity;
        }
    }

    private static class StorageBlockEntryImpl implements StorageBlockEntry {
        private long oneKEnergyUsage = DefaultEnergyUsage.ONE_K_STORAGE_BLOCK;
        private long fourKEnergyUsage = DefaultEnergyUsage.FOUR_K_STORAGE_BLOCK;
        private long sixteenKEnergyUsage = DefaultEnergyUsage.SIXTEEN_K_STORAGE_BLOCK;
        private long sixtyFourKEnergyUsage = DefaultEnergyUsage.SIXTY_FOUR_K_STORAGE_BLOCK;
        private long creativeEnergyUsage = DefaultEnergyUsage.CREATIVE_STORAGE_BLOCK;

        @Override
        public long get1kEnergyUsage() {
            return oneKEnergyUsage;
        }

        @Override
        public long get4kEnergyUsage() {
            return fourKEnergyUsage;
        }

        @Override
        public long get16kEnergyUsage() {
            return sixteenKEnergyUsage;
        }

        @Override
        public long get64kEnergyUsage() {
            return sixtyFourKEnergyUsage;
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeEnergyUsage;
        }
    }

    private static class FluidStorageBlockEntryImpl implements FluidStorageBlockEntry {
        private long sixtyFourBEnergyUsage = DefaultEnergyUsage.SIXTY_FOUR_B_FLUID_STORAGE_BLOCK;
        private long twoHundredFiftySixBEnergyUsage = DefaultEnergyUsage.TWO_HUNDRED_FIFTY_SIX_B_FLUID_STORAGE_BLOCK;
        private long thousandTwentyFourBEnergyUsage = DefaultEnergyUsage.THOUSAND_TWENTY_FOUR_B_FLUID_STORAGE_BLOCK;
        private long fourThousandNinetySixBEnergyUsage =
            DefaultEnergyUsage.FOUR_THOUSAND_NINETY_SIX_B_FLUID_STORAGE_BLOCK;
        private long creativeEnergyUsage = DefaultEnergyUsage.CREATIVE_FLUID_STORAGE_BLOCK;

        @Override
        public long get64bEnergyUsage() {
            return sixtyFourBEnergyUsage;
        }

        @Override
        public long get256bEnergyUsage() {
            return twoHundredFiftySixBEnergyUsage;
        }

        @Override
        public long get1024bEnergyUsage() {
            return thousandTwentyFourBEnergyUsage;
        }

        @Override
        public long get4096bEnergyUsage() {
            return fourThousandNinetySixBEnergyUsage;
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeEnergyUsage;
        }
    }

    private static class UpgradeEntryImpl implements UpgradeEntry {
        private long speedUpgradeEnergyUsage = DefaultEnergyUsage.SPEED_UPGRADE;

        private long stackUpgradeEnergyUsage = DefaultEnergyUsage.STACK_UPGRADE;

        private long fortune1UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_1_UPGRADE;

        private long fortune2UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_2_UPGRADE;

        private long fortune3UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_3_UPGRADE;

        private long silkTouchUpgradeEnergyUsage = DefaultEnergyUsage.SILK_TOUCH_UPGRADE;

        private long regulatorUpgradeEnergyUsage = DefaultEnergyUsage.REGULATOR_UPGRADE;

        private long rangeUpgradeEnergyUsage = DefaultEnergyUsage.RANGE_UPGRADE;

        private long creativeRangeUpgradeEnergyUsage = DefaultEnergyUsage.CREATIVE_RANGE_UPGRADE;

        private int rangeUpgradeRange = DefaultEnergyUsage.RANGE_UPGRADE_RANGE;

        private int autocraftingUpgradeRange = DefaultEnergyUsage.AUTOCRAFTING_UPGRADE;

        @Override
        public long getSpeedUpgradeEnergyUsage() {
            return speedUpgradeEnergyUsage;
        }

        @Override
        public long getStackUpgradeEnergyUsage() {
            return stackUpgradeEnergyUsage;
        }

        @Override
        public long getFortune1UpgradeEnergyUsage() {
            return fortune1UpgradeEnergyUsage;
        }

        @Override
        public long getFortune2UpgradeEnergyUsage() {
            return fortune2UpgradeEnergyUsage;
        }

        @Override
        public long getFortune3UpgradeEnergyUsage() {
            return fortune3UpgradeEnergyUsage;
        }

        @Override
        public long getSilkTouchUpgradeEnergyUsage() {
            return silkTouchUpgradeEnergyUsage;
        }

        @Override
        public long getRegulatorUpgradeEnergyUsage() {
            return regulatorUpgradeEnergyUsage;
        }

        @Override
        public long getRangeUpgradeEnergyUsage() {
            return rangeUpgradeEnergyUsage;
        }

        @Override
        public long getCreativeRangeUpgradeEnergyUsage() {
            return creativeRangeUpgradeEnergyUsage;
        }

        @Override
        public int getRangeUpgradeRange() {
            return rangeUpgradeRange;
        }

        @Override
        public long getAutocraftingUpgradeEnergyUsage() {
            return autocraftingUpgradeRange;
        }
    }

    private static class WirelessGridEntryImpl implements WirelessGridEntry {
        private long energyCapacity = DefaultEnergyUsage.WIRELESS_GRID_CAPACITY;

        private long openEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_OPEN;

        private long insertEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_INSERT;

        private long extractEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_EXTRACT;

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage;
        }

        @Override
        public long getInsertEnergyUsage() {
            return insertEnergyUsage;
        }

        @Override
        public long getExtractEnergyUsage() {
            return extractEnergyUsage;
        }
    }

    private static class WirelessTransmitterEntryImpl implements WirelessTransmitterEntry {
        private long energyUsage = DefaultEnergyUsage.WIRELESS_TRANSMITTER;

        private int baseRange = DefaultEnergyUsage.WIRELESS_TRANSMITTER_BASE_RANGE;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public int getBaseRange() {
            return baseRange;
        }
    }

    private static class PortableGridEntryImpl implements PortableGridEntry {
        private long energyCapacity = DefaultEnergyUsage.PORTABLE_GRID_CAPACITY;

        private long openEnergyUsage = DefaultEnergyUsage.PORTABLE_GRID_OPEN;

        private long insertEnergyUsage = DefaultEnergyUsage.PORTABLE_GRID_INSERT;

        private long extractEnergyUsage = DefaultEnergyUsage.PORTABLE_GRID_EXTRACT;

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage;
        }

        @Override
        public long getInsertEnergyUsage() {
            return insertEnergyUsage;
        }

        @Override
        public long getExtractEnergyUsage() {
            return extractEnergyUsage;
        }
    }

    private static class RelayEntryImpl implements RelayEntry {
        private long inputNetworkEnergyUsage = DefaultEnergyUsage.RELAY_INPUT_NETWORK;

        private long outputNetworkEnergyUsage = DefaultEnergyUsage.RELAY_OUTPUT_NETWORK;

        @Override
        public long getInputNetworkEnergyUsage() {
            return inputNetworkEnergyUsage;
        }

        @Override
        public long getOutputNetworkEnergyUsage() {
            return outputNetworkEnergyUsage;
        }
    }

    private static class AutocrafterEntryImpl implements AutocrafterEntry {
        private long energyUsage = DefaultEnergyUsage.AUTOCRAFTER;

        private long energyUsagePerPattern = DefaultEnergyUsage.AUTOCRAFTER_PER_PATTERN;

        @Override
        public long getEnergyUsagePerPattern() {
            return energyUsagePerPattern;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class AutocrafterManagerEntryImpl implements AutocrafterManagerEntry {
        private long energyUsage = DefaultEnergyUsage.AUTOCRAFTER_MANAGER;

        private AutocrafterManagerSearchMode searchMode = AutocrafterManagerSearchMode.ALL;

        private AutocrafterManagerViewType viewType = AutocrafterManagerViewType.VISIBLE;

        @Override
        public void setSearchMode(final AutocrafterManagerSearchMode searchMode) {
            this.searchMode = searchMode;
            save();
        }

        @Override
        public AutocrafterManagerSearchMode getSearchMode() {
            return searchMode;
        }

        @Override
        public void setViewType(final AutocrafterManagerViewType viewType) {
            this.viewType = viewType;
            save();
        }

        @Override
        public AutocrafterManagerViewType getViewType() {
            return viewType;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        private static void save() {
            AutoConfig.getConfigHolder(ConfigImpl.class).save();
        }
    }

    private static class WirelessAutocraftingMonitorEntryImpl implements WirelessAutocraftingMonitorEntry {
        private long energyCapacity = DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CAPACITY;

        private long openEnergyUsage = DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_OPEN;

        private long cancelEnergyUsage = DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CANCEL;

        private long cancelAllEnergyUsage = DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CANCEL_ALL;

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage;
        }

        @Override
        public long getCancelEnergyUsage() {
            return cancelEnergyUsage;
        }

        @Override
        public long getCancelAllEnergyUsage() {
            return cancelAllEnergyUsage;
        }
    }
}
