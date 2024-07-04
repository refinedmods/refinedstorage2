package com.refinedmods.refinedstorage.platform.common.content;

import com.refinedmods.refinedstorage.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.controller.ControllerBlockItem;
import com.refinedmods.refinedstorage.platform.common.controller.CreativeControllerBlockItem;
import com.refinedmods.refinedstorage.platform.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.platform.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.platform.common.security.FallbackSecurityCardItem;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardItem;
import com.refinedmods.refinedstorage.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage.platform.common.storage.portablegrid.PortableGridBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.BaseBlockItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    private final Map<ItemStorageType.Variant, Supplier<Item>> itemStorageParts
        = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<Item>> fluidStorageParts
        = new EnumMap<>(FluidStorageType.Variant.class);
    private final Map<ItemStorageType.Variant, Supplier<Item>> itemStorageDisks
        = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<Item>> fluidStorageDisks
        = new EnumMap<>(FluidStorageType.Variant.class);
    private final List<Supplier<ControllerBlockItem>> allControllers = new ArrayList<>();
    private final List<Supplier<CreativeControllerBlockItem>> allCreativeControllers = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allCables = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allExporters = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allImporters = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allExternalStorages = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allDetectors = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allDestructors = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allConstructors = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allWirelessTransmitters = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allNetworkReceivers = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allNetworkTransmitters = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allSecurityManagers = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allRelays = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allDiskInterfaces = new ArrayList<>();
    @Nullable
    private Supplier<Item> quartzEnrichedIron;
    @Nullable
    private Supplier<Item> quartzEnrichedCopper;
    @Nullable
    private Supplier<Item> silicon;
    @Nullable
    private Supplier<Item> processorBinding;
    @Nullable
    private Supplier<Item> wrench;
    private final Map<ProcessorItem.Type, Supplier<Item>> processors = new EnumMap<>(ProcessorItem.Type.class);
    @Nullable
    private Supplier<Item> constructionCore;
    @Nullable
    private Supplier<Item> destructionCore;
    @Nullable
    private Supplier<Item> storageHousing;
    @Nullable
    private Supplier<Item> upgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> speedUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> stackUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> fortune1Upgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> fortune2Upgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> fortune3Upgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> silkTouchUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> regulatorUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> rangeUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> creativeRangeUpgrade;
    @Nullable
    private Supplier<WirelessGridItem> wirelessGrid;
    @Nullable
    private Supplier<WirelessGridItem> creativeWirelessGrid;
    @Nullable
    private Supplier<Item> configurationCard;
    @Nullable
    private Supplier<Item> networkCard;
    @Nullable
    private Supplier<PortableGridBlockItem> portableGrid;
    @Nullable
    private Supplier<PortableGridBlockItem> creativePortableGrid;
    @Nullable
    private Supplier<SecurityCardItem> securityCard;
    @Nullable
    private Supplier<FallbackSecurityCardItem> fallbackSecurityCard;

    private Items() {
    }

    public Item getItemStoragePart(final ItemStorageType.Variant variant) {
        return itemStorageParts.get(variant).get();
    }

    public void setItemStoragePart(final ItemStorageType.Variant variant, final Supplier<Item> supplier) {
        itemStorageParts.put(variant, supplier);
    }

    public Item getItemStorageDisk(final ItemStorageType.Variant variant) {
        return itemStorageDisks.get(variant).get();
    }

    public void setItemStorageDisk(final ItemStorageType.Variant variant, final Supplier<Item> supplier) {
        itemStorageDisks.put(variant, supplier);
    }

    public Item getFluidStoragePart(final FluidStorageType.Variant type) {
        return fluidStorageParts.get(type).get();
    }

    public void setFluidStoragePart(final FluidStorageType.Variant variant, final Supplier<Item> supplier) {
        fluidStorageParts.put(variant, supplier);
    }

    public Item getFluidStorageDisk(final FluidStorageType.Variant variant) {
        return fluidStorageDisks.get(variant).get();
    }

    public void setFluidStorageDisk(final FluidStorageType.Variant variant, final Supplier<Item> supplier) {
        fluidStorageDisks.put(variant, supplier);
    }

    public void setQuartzEnrichedIron(final Supplier<Item> supplier) {
        this.quartzEnrichedIron = supplier;
    }

    public Item getQuartzEnrichedIron() {
        return requireNonNull(quartzEnrichedIron).get();
    }

    public void setQuartzEnrichedCopper(final Supplier<Item> supplier) {
        this.quartzEnrichedCopper = supplier;
    }

    public Item getQuartzEnrichedCopper() {
        return requireNonNull(quartzEnrichedCopper).get();
    }

    public void setSilicon(final Supplier<Item> supplier) {
        this.silicon = supplier;
    }

    public Item getSilicon() {
        return requireNonNull(silicon).get();
    }

    public void setProcessorBinding(final Supplier<Item> supplier) {
        this.processorBinding = supplier;
    }

    public Item getProcessorBinding() {
        return requireNonNull(processorBinding).get();
    }

    public void setWrench(final Supplier<Item> supplier) {
        this.wrench = supplier;
    }

    public Item getWrench() {
        return requireNonNull(wrench).get();
    }

    public void setProcessor(final ProcessorItem.Type type, final Supplier<Item> supplier) {
        this.processors.put(type, supplier);
    }

    public Item getProcessor(final ProcessorItem.Type type) {
        return requireNonNull(processors.get(type)).get();
    }

    public void setConstructionCore(final Supplier<Item> supplier) {
        this.constructionCore = supplier;
    }

    public Item getConstructionCore() {
        return requireNonNull(constructionCore).get();
    }

    public void setDestructionCore(final Supplier<Item> supplier) {
        this.destructionCore = supplier;
    }

    public Item getDestructionCore() {
        return requireNonNull(destructionCore).get();
    }

    public void addCreativeController(final Supplier<CreativeControllerBlockItem> supplier) {
        allCreativeControllers.add(supplier);
    }

    public List<Supplier<CreativeControllerBlockItem>> getCreativeControllers() {
        return Collections.unmodifiableList(allCreativeControllers);
    }

    public void addController(final Supplier<ControllerBlockItem> supplier) {
        allControllers.add(supplier);
    }

    public List<Supplier<ControllerBlockItem>> getControllers() {
        return Collections.unmodifiableList(allControllers);
    }

    public void addCable(final Supplier<BaseBlockItem> supplier) {
        allCables.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getCables() {
        return Collections.unmodifiableList(allCables);
    }

    public void addExporter(final Supplier<BaseBlockItem> supplier) {
        allExporters.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getExporters() {
        return Collections.unmodifiableList(allExporters);
    }

    public void addImporter(final Supplier<BaseBlockItem> supplier) {
        allImporters.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getImporters() {
        return Collections.unmodifiableList(allImporters);
    }

    public void addExternalStorage(final Supplier<BaseBlockItem> supplier) {
        allExternalStorages.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getExternalStorages() {
        return Collections.unmodifiableList(allExternalStorages);
    }

    public void addDetector(final Supplier<BaseBlockItem> supplier) {
        allDetectors.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getDetectors() {
        return Collections.unmodifiableList(allDetectors);
    }

    public void addDestructor(final Supplier<BaseBlockItem> supplier) {
        allDestructors.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getDestructors() {
        return Collections.unmodifiableList(allDestructors);
    }

    public void addConstructor(final Supplier<BaseBlockItem> supplier) {
        allConstructors.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getConstructors() {
        return Collections.unmodifiableList(allConstructors);
    }

    public void addWirelessTransmitter(final Supplier<BaseBlockItem> supplier) {
        allWirelessTransmitters.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getWirelessTransmitters() {
        return Collections.unmodifiableList(allWirelessTransmitters);
    }

    public Item getStorageHousing() {
        return requireNonNull(storageHousing).get();
    }

    public void setStorageHousing(final Supplier<Item> supplier) {
        this.storageHousing = supplier;
    }

    public void setUpgrade(final Supplier<Item> supplier) {
        this.upgrade = supplier;
    }

    public Item getUpgrade() {
        return requireNonNull(upgrade).get();
    }

    public AbstractUpgradeItem getSpeedUpgrade() {
        return requireNonNull(speedUpgrade).get();
    }

    public void setSpeedUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.speedUpgrade = supplier;
    }

    public AbstractUpgradeItem getStackUpgrade() {
        return requireNonNull(stackUpgrade).get();
    }

    public void setStackUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.stackUpgrade = supplier;
    }

    public AbstractUpgradeItem getFortune1Upgrade() {
        return requireNonNull(fortune1Upgrade).get();
    }

    public void setFortune1Upgrade(final Supplier<AbstractUpgradeItem> fortune1Upgrade) {
        this.fortune1Upgrade = fortune1Upgrade;
    }

    public AbstractUpgradeItem getFortune2Upgrade() {
        return requireNonNull(fortune2Upgrade).get();
    }

    public void setFortune2Upgrade(final Supplier<AbstractUpgradeItem> fortune2Upgrade) {
        this.fortune2Upgrade = fortune2Upgrade;
    }

    public AbstractUpgradeItem getFortune3Upgrade() {
        return requireNonNull(fortune3Upgrade).get();
    }

    public void setFortune3Upgrade(final Supplier<AbstractUpgradeItem> fortune3Upgrade) {
        this.fortune3Upgrade = fortune3Upgrade;
    }

    public AbstractUpgradeItem getSilkTouchUpgrade() {
        return requireNonNull(silkTouchUpgrade).get();
    }

    public void setSilkTouchUpgrade(final Supplier<AbstractUpgradeItem> silkTouchUpgrade) {
        this.silkTouchUpgrade = silkTouchUpgrade;
    }

    public AbstractUpgradeItem getRegulatorUpgrade() {
        return requireNonNull(regulatorUpgrade).get();
    }

    public void setRegulatorUpgrade(final Supplier<AbstractUpgradeItem> regulatorUpgrade) {
        this.regulatorUpgrade = regulatorUpgrade;
    }

    public AbstractUpgradeItem getRangeUpgrade() {
        return requireNonNull(rangeUpgrade).get();
    }

    public void setRangeUpgrade(final Supplier<AbstractUpgradeItem> rangeUpgrade) {
        this.rangeUpgrade = rangeUpgrade;
    }

    public AbstractUpgradeItem getCreativeRangeUpgrade() {
        return requireNonNull(creativeRangeUpgrade).get();
    }

    public void setCreativeRangeUpgrade(final Supplier<AbstractUpgradeItem> creativeRangeUpgrade) {
        this.creativeRangeUpgrade = creativeRangeUpgrade;
    }

    public WirelessGridItem getWirelessGrid() {
        return requireNonNull(wirelessGrid).get();
    }

    public void setWirelessGrid(final Supplier<WirelessGridItem> supplier) {
        this.wirelessGrid = supplier;
    }

    public WirelessGridItem getCreativeWirelessGrid() {
        return requireNonNull(creativeWirelessGrid).get();
    }

    public void setCreativeWirelessGrid(final Supplier<WirelessGridItem> supplier) {
        this.creativeWirelessGrid = supplier;
    }

    public Item getConfigurationCard() {
        return requireNonNull(configurationCard).get();
    }

    public void setConfigurationCard(final Supplier<Item> supplier) {
        this.configurationCard = supplier;
    }

    public void addNetworkReceiver(final Supplier<BaseBlockItem> supplier) {
        allNetworkReceivers.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getNetworkReceivers() {
        return Collections.unmodifiableList(allNetworkReceivers);
    }

    public void addNetworkTransmitter(final Supplier<BaseBlockItem> supplier) {
        allNetworkTransmitters.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getNetworkTransmitters() {
        return Collections.unmodifiableList(allNetworkTransmitters);
    }

    public void addSecurityManager(final Supplier<BaseBlockItem> supplier) {
        allSecurityManagers.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getSecurityManagers() {
        return Collections.unmodifiableList(allSecurityManagers);
    }

    public void addRelay(final Supplier<BaseBlockItem> supplier) {
        allRelays.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getRelays() {
        return Collections.unmodifiableList(allRelays);
    }

    public void addDiskInterface(final Supplier<BaseBlockItem> supplier) {
        allDiskInterfaces.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getDiskInterfaces() {
        return Collections.unmodifiableList(allDiskInterfaces);
    }

    public Item getNetworkCard() {
        return requireNonNull(networkCard).get();
    }

    public void setNetworkCard(final Supplier<Item> supplier) {
        this.networkCard = supplier;
    }

    public PortableGridBlockItem getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<PortableGridBlockItem> supplier) {
        this.portableGrid = supplier;
    }

    public PortableGridBlockItem getCreativePortableGrid() {
        return requireNonNull(creativePortableGrid).get();
    }

    public void setCreativePortableGrid(final Supplier<PortableGridBlockItem> supplier) {
        this.creativePortableGrid = supplier;
    }

    public SecurityCardItem getSecurityCard() {
        return requireNonNull(securityCard).get();
    }

    public void setSecurityCard(final Supplier<SecurityCardItem> securityCard) {
        this.securityCard = securityCard;
    }

    public FallbackSecurityCardItem getFallbackSecurityCard() {
        return requireNonNull(fallbackSecurityCard).get();
    }

    public void setFallbackSecurityCard(final Supplier<FallbackSecurityCardItem> fallbackSecurityCard) {
        this.fallbackSecurityCard = fallbackSecurityCard;
    }
}
