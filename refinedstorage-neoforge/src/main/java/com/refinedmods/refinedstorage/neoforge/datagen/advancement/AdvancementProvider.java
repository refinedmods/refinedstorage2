package com.refinedmods.refinedstorage.neoforge.datagen.advancement;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.Tags;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_NAME;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(final HolderLookup.Provider registries, final Consumer<AdvancementHolder> consumer) {
        final var items = registries.lookupOrThrow(Registries.ITEM);

        final var root = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getCreativeController().getDefault(),
                MOD_NAME,
                createTranslation("advancements", "root.description"),
                createIdentifier("gui/advancements"),
                AdvancementType.TASK,
                false,
                true,
                false
            )
            .addCriterion("controller_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.CONTROLLERS).build()
            ))
            .save(consumer, MOD_ID + ":root");

        final var connecting = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getCable().getDefault(),
                createTranslation("advancements", "connecting"),
                createTranslation("advancements", "connecting.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("cable_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.CABLES).build()
            ))
            .save(consumer, MOD_ID + ":connecting");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getRelay().getDefault(),
                createTranslation("advancements", "conditional_connecting"),
                createTranslation("advancements", "conditional_connecting.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(connecting)
            .addCriterion("relay_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.RELAYS).build()
            ))
            .save(consumer, MOD_ID + ":conditional_connecting");

        final var drives = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getDiskDrive(),
                createTranslation("advancements", "drives"),
                createTranslation("advancements", "drives.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("disk_drive_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Blocks.INSTANCE.getDiskDrive()
            ))
            .save(consumer, MOD_ID + ":drives");

        final var storingItems = Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getItemStorageDisk(ItemStorageVariant.ONE_K),
                createTranslation("advancements", "storing_items"),
                createTranslation("advancements", "storing_items.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(drives)
            .addCriterion("storage_disk_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.STORAGE_DISKS).build()
            ))
            .save(consumer, MOD_ID + ":storing_items");

        Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getFluidStorageDisk(FluidStorageVariant.SIXTY_FOUR_B),
                createTranslation("advancements", "storing_fluids"),
                createTranslation("advancements", "storing_fluids.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(drives)
            .addCriterion("fluid_storage_disk_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.FLUID_STORAGE_DISKS).build()
            ))
            .save(consumer, MOD_ID + ":storing_fluids");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getDiskInterface().getDefault(),
                createTranslation("advancements", "interfacing_with_disks"),
                createTranslation("advancements", "interfacing_with_disks.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(drives)
            .addCriterion("disk_interface_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.DISK_INTERFACES).build()
            ))
            .save(consumer, MOD_ID + ":interfacing_with_disks");

        final var viewingYourStorage = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getGrid().getDefault(),
                createTranslation("advancements", "viewing_your_storage"),
                createTranslation("advancements", "viewing_your_storage.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(storingItems)
            .addCriterion("grid_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.GRIDS).build()
            ))
            .save(consumer, MOD_ID + ":viewing_your_storage");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getCraftingGrid().getDefault(),
                createTranslation("advancements", "upgrading_your_grid"),
                createTranslation("advancements", "upgrading_your_grid.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(viewingYourStorage)
            .addCriterion("crafting_grid_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.CRAFTING_GRIDS).build()
            ))
            .save(consumer, MOD_ID + ":upgrading_your_grid");

        Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getPortableGrid(),
                createTranslation("advancements", "portable_storage"),
                createTranslation("advancements", "portable_storage.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(viewingYourStorage)
            .addCriterion("portable_grid_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.INSTANCE.getPortableGrid()
            ))
            .save(consumer, MOD_ID + ":portable_storage");

        final var exporting = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getExporter().getDefault(),
                createTranslation("advancements", "exporting"),
                createTranslation("advancements", "exporting.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("exporter_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.EXPORTERS).build()
            ))
            .save(consumer, MOD_ID + ":exporting");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getConstructor().getDefault(),
                createTranslation("advancements", "construction"),
                createTranslation("advancements", "construction.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(exporting)
            .addCriterion("constructor_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.CONSTRUCTORS).build()
            ))
            .save(consumer, MOD_ID + ":construction");

        final var importing = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getImporter().getDefault(),
                createTranslation("advancements", "importing"),
                createTranslation("advancements", "importing.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("importer_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.IMPORTERS).build()
            ))
            .save(consumer, MOD_ID + ":importing");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getDestructor().getDefault(),
                createTranslation("advancements", "destruction"),
                createTranslation("advancements", "destruction.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(importing)
            .addCriterion("destructor_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.DESTRUCTORS).build()
            ))
            .save(consumer, MOD_ID + ":destruction");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getDetector().getDefault(),
                createTranslation("advancements", "detecting"),
                createTranslation("advancements", "detecting.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("detector_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.DETECTORS).build()
            ))
            .save(consumer, MOD_ID + ":detecting");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getExternalStorage().getDefault(),
                createTranslation("advancements", "storing_externally"),
                createTranslation("advancements", "storing_externally.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("external_storage_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.EXTERNAL_STORAGES).build()
            ))
            .save(consumer, MOD_ID + ":storing_externally");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getStorageMonitor(),
                createTranslation("advancements", "better_than_a_barrel"),
                createTranslation("advancements", "better_than_a_barrel.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("storage_monitor_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Blocks.INSTANCE.getStorageMonitor()
            ))
            .save(consumer, MOD_ID + ":better_than_a_barrel");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getInterface(),
                createTranslation("advancements", "interface_to_the_world"),
                createTranslation("advancements", "interface_to_the_world.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("interface_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Blocks.INSTANCE.getInterface()
            ))
            .save(consumer, MOD_ID + ":interface_to_the_world");

        final var wireless = Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getWirelessGrid(),
                createTranslation("advancements", "wireless"),
                createTranslation("advancements", "wireless.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("wireless_grid_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Items.INSTANCE.getWirelessGrid()).build(),
                ItemPredicate.Builder.item().of(items, Tags.WIRELESS_TRANSMITTERS).build()
            ))
            .save(consumer, MOD_ID + ":wireless");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getNetworkTransmitter().getDefault(),
                createTranslation("advancements", "no_cables_required"),
                createTranslation("advancements", "no_cables_required.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(wireless)
            .addCriterion("network_transmitter_receiver_card_in_inventory",
                InventoryChangeTrigger.TriggerInstance.hasItems(
                    ItemPredicate.Builder.item().of(items, Tags.NETWORK_TRANSMITTERS).build(),
                    ItemPredicate.Builder.item().of(items, Tags.NETWORK_RECEIVERS).build(),
                    ItemPredicate.Builder.item().of(items, Items.INSTANCE.getNetworkCard()).build()
                ))
            .save(consumer, MOD_ID + ":no_cables_required");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getSecurityManager().getDefault(),
                createTranslation("advancements", "security"),
                createTranslation("advancements", "security.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("security_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.SECURITY_MANAGERS).build(),
                ItemPredicate.Builder.item()
                    .of(items, Items.INSTANCE.getSecurityCard(), Items.INSTANCE.getFallbackSecurityCard())
                    .build()
            ))
            .save(consumer, MOD_ID + ":security");

        Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getUpgrade(),
                createTranslation("advancements", "upgrading"),
                createTranslation("advancements", "upgrading.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("upgrade_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.INSTANCE.getUpgrade()
            ))
            .save(consumer, MOD_ID + ":upgrading");

        final var autocrafting = Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getAutocrafter().getDefault(),
                createTranslation("advancements", "autocrafting"),
                createTranslation("advancements", "autocrafting.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(root)
            .addCriterion("autocrafter_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.PATTERN_GRIDS).build(),
                ItemPredicate.Builder.item().of(items, Tags.AUTOCRAFTERS).build(),
                ItemPredicate.Builder.item().of(items, Items.INSTANCE.getPattern()).build()
            ))
            .save(consumer, MOD_ID + ":autocrafting");

        Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getAutocraftingUpgrade(),
                createTranslation("advancements", "autocrafting_on_demand"),
                createTranslation("advancements", "autocrafting_on_demand.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(autocrafting)
            .addCriterion("autocrafting_upgrade_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.INSTANCE.getAutocraftingUpgrade()
            ))
            .save(consumer, MOD_ID + ":autocrafting_on_demand");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getAutocrafterManager().getDefault(),
                createTranslation("advancements", "managing_patterns"),
                createTranslation("advancements", "managing_patterns.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(autocrafting)
            .addCriterion("autocrafter_manager_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.AUTOCRAFTER_MANAGERS).build()
            ))
            .save(consumer, MOD_ID + ":managing_patterns");

        Advancement.Builder.advancement()
            .display(
                Blocks.INSTANCE.getAutocraftingMonitor().getDefault(),
                createTranslation("advancements", "monitoring"),
                createTranslation("advancements", "monitoring.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(autocrafting)
            .addCriterion("autocrafting_monitor_in_inventory", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(items, Tags.AUTOCRAFTING_MONITORS).build()
            ))
            .save(consumer, MOD_ID + ":monitoring");

        Advancement.Builder.advancement()
            .display(
                Items.INSTANCE.getWirelessAutocraftingMonitor(),
                createTranslation("advancements", "wireless_monitoring"),
                createTranslation("advancements", "wireless_monitoring.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .parent(wireless)
            .addCriterion("wireless_autocrafting_monitor_in_inventory",
                InventoryChangeTrigger.TriggerInstance.hasItems(
                    Items.INSTANCE.getWirelessAutocraftingMonitor()
                ))
            .save(consumer, MOD_ID + ":wireless_monitoring");
    }
}
