# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

-   For keybindings on Mac, CMD is now always used instead of CTRL.
-   Missing translation key for Refined Storage controls.
-   Missing background sprite on bulk autocrafting request buttons.
-   Grid scrolling between grid and inventory always going to inventory.

## [3.0.0-beta.1] - 2026-04-13

### Added

-   Ported to Minecraft 26.1.2.

### Fixed

-   Correct the torch model for detectors.

## [2.0.1] - 2026-02-11

### Added

-   Support for underscores and forward slashes in the Grid search.
-   Support for searching for block tags on block items in the Grid search.

### Fixed

-   Improved performance in various scenarios like autocrafting and opening a Grid.
-   Autocrafter name field history not working.
-   The help descriptions of '#' and '$' prefix searches being swapped.

## [2.0.0] - 2025-09-27

### Fixed

-   Improved autocrafting toast texture.

## [2.0.0-beta.17] - 2025-09-27

## [2.0.0-beta.16] - 2025-09-26

### Fixed

-   Cables being disconnected visually when loading a world.
-   Processing patterns inserting ingredient types in the wrong order into machines.
-   Multiple Regulator Upgrades for different resources not exporting anything with "Default" scheduling mode.
-   Cancelling an in-progress processing crafting task not giving back resources from intermediate crafting storage, if more of that resource was still expected at the time of cancellation.
-   Performance issue when bulk crafting with remainder items in the Crafting Grid.

## [2.0.0-beta.15] - 2025-09-24

### Fixed

-   Crash when a pattern cycle occurs with an Autocrafting Upgrade.

## [2.0.0-beta.14] - 2025-09-20

### Fixed

-   The storage network losing track of resources that can be autocrafted.
-   Reduced lag spikes when Network Receivers are missing.
-   The mod not starting up on Fabric due to an invalid TeamReborn Energy version.
-   Centered mouse interaction with the scrollbar to feel more natural.
-   Pattern Grid in processing mode slots being cut off on a certain position.

## [2.0.0-beta.13] - 2025-09-13

### Fixed

-   Fixed pattern crash for recipes with no ingredients specified.

## [2.0.0-beta.12] - 2025-09-12

### Added

-   Duplicating storage disks with middle click in creative mode will now give a new storage disk instead of referencing the copied one.

### Fixed

-   Rare crash when starting an autocrafting task.
-   Resource containers not dealing with registry entries from removed mods properly.
-   Various autocrafting performance problems.
-   Autocrafter voiding all contained patterns when a pattern became invalid due to removing a mod.

## [2.0.0-beta.11] - 2025-08-25

### Added

-   The resource type side button now shows a warning when the Grid is filtering by resource type and there are no resources for that type to display.
-   The Autocrafting Monitor now shows the state of the crafting task (ready, extracting initial resources, running, returning internal storage and completed). If a task is not able to run or complete, this should give more insight as to why.
-   The Autocrafting Monitor now shows which initial resources still have to be extracted from the storage before the task can start. Most of the time this is instant and won't be visible to the user, but can help with understanding stuck tasks when combining autocrafting with external storages that report fake items like Functional Storage's Compacting Storage.

### Fixed

-   Autocrafting not properly differentiating between outputs and byproducts and calculating the wrong task when requesting a byproduct of another pattern.
-   Reduce lag spikes when opening a Grid with many item types.

## [2.0.0-beta.10] - 2025-08-19

### Fixed

-   Duplication bug with Inventory Essentials.
-   Incorrect extractable amount shown when hovering over a fluid tank in the Grid.
-   Crash when crafting tree preview is requested and the same resource amount is being used over multiple branches.
-   Fixed processing patterns inserting ingredients in the wrong order into machines.

## [2.0.0-beta.9] - 2025-08-15

### Fixed

-   Fixed Crafting Grid failing to pull additional items for crafts with External Storage inventories that have more than the maximum stack size in 1 slot.
-   Fixed Interface missing resource tooltip not disappearing after clearing slot configuration.
-   Fixed storage disk info not updating properly in tooltip and Disk Drive.
-   Implemented a fallback mechanism for resources that cannot be extracted from the Grid due to an incorrect data component implementation in the other mod.

## [2.0.0-beta.8] - 2025-08-08

### Fixed

-   Fixed Grid not showing last modified date after reopening GUI.
-   Fixed not being able to extract fluids from External Storage in the Grid on Fabric.
-   Fixed mirrored autocrafting patterns in fuzzy mode causing a crash.
-   Fixed Network Transmitter reconnecting with any type of Refined Storage block.

## [2.0.0-beta.7] - 2025-08-05

### Added

-   Added Debug Stick and network debug overlay, enabled with a config option (NeoForge-only).

### Fixed

-   Fixed crash when closing a Wireless Grid that became available after opening the GUI.
-   Fixed networks breaking when modifying the receiving end when using Network Transmitters and Network Receivers.
-   Rotating Relays was disabled since v2.0.0-milestone.4.14 because of a crash. This crash has now been fixed and Relays can once again be rotated.

## [2.0.0-beta.6] - 2025-08-03

### Changed

-   Opened GUIs will now automatically close if you go out of reach or the block is destroyed.
-   Quartz Enriched Iron and Copper are now part of the `c:ingots` item tag.

### Fixed

-   Fixed Pattern output not rendering sometimes.
-   Fixed patterns being displaced after scrolling and clicking in the search bar of the Autocrafter Manager.
-   Fixed crash when exploding TNT next to an opened Grid.
-   Fixed crafting events not being triggered from the Crafting Grid.

## [2.0.0-beta.5] - 2025-07-31

### Changed

-   You need to crouch now to place the Portable Grid in the world.

### Fixed

-   Fixed item quantities being able to overlap when using mB.
-   Fixed crash with Exporter when the connected inventory incorrectly reported a higher remainder than the originally inserted amount.
-   Fixed being able to move the Portable Grid to another inventory slot when using a hotkey.
-   Fixed Portable Grid losing energy when switching disks.
-   Fixed crash when submerging Detector in water.
-   Fixed Interface not dropping upgrades.

## [2.0.0-beta.4] - 2025-07-29

### Added

-   The crafting preview can now show a crafting tree.

### Fixed

-   The Interface's resource container is protected against empty fluid stacks now, avoiding crashes with certain mods.

## [2.0.0-beta.3] - 2025-07-22

### Changed

-   Closing the Crafting Preview window (via cancel or escape) will now properly cancel pending calculations.
-   Autocrafting requests that take too long will be automatically stopped after 5 seconds.
-   It is no longer possible to queue an unlimited amount of autocrafting requests when the autocrafting system is busy. 

## [2.0.0-beta.2] - 2025-04-06

### Changed

-   Updated Disk Drive textures.
-   Updated Machine Casing textures.
-   Updated Crafting Grid textures.
-   Updated Storage Monitor textures.
-   Updated Controller casing textures.
-   Updated Autocrafter casing textures.
-   Updated Configuration Card, Processor Binding, Silicon, Quartz Enriched Iron and Quartz Enriched Copper textures.

### Fixed

-   Fixed Relay always requiring 8 energy on NeoForge.
-   Fixed crash with processing patterns that have no outputs due to removing mods.
-   Fixed not being able to shift click items in the Crafting Grid when an External Storage was connected, on NeoForge.
-   Fixed locking mode on the Autocrafter "Lock until connected machine is empty" inserting too many resources when using speed upgrades.
-   Fixed processing and crafting pattern tooltips sometimes not being large enough.
-   Fixed crash when adding pattern for EnderIO conduits in single player.
-   Fixed Portable Grid and Controller doubling stored energy when placed.

## [2.0.0-beta.1] - 2025-03-28

### Changed

-   The External Storage now supports multiple resource types on a single connected inventory.
-   Tooltips for upgrade slots are now ordered.
-   Grid tag search now uses the `#` prefix, tooltips use the `$` prefix.

### Fixed

-   Fixed cables broken with a Wrench not stacking with newly crafted cables.
-   Fixed slow performance in the Grid when searching.
-   Fixed storages failing to load after removing an addon that adds more storage types.
-   Fixed crash when trying to open block in spectator mode.
-   Fixed crash when trying to open Grid with EnderIO autocrafting pattern in network.
-   Fixed External Storage making resources flicker when the connected inventory is causing neighbor updates.
-   Fixed not being able to search for Water Bottles.
-   Fixed crash when creating a Pattern for a shield.

## [2.0.0-milestone.4.14] - 2025-02-23

### Added

-   Autocrafting Upgrade

### Changed

-   The filter slots for the Exporter, Constructor and Interface now display whether a resource is missing, the destination does not accept it, the resource cannot be autocrafted due to missing resources, or whether the resource is currently autocrafting.
-   'B' is now displayed after fluid amounts, indicating the amount in buckets.
-   The Autocrafter now only connects to other autocrafters through its front face. The reason for this is so that you can connect it to an Interface and accept multi-type autocrafting inputs without the Interface being connected to your network.
-   When using Autocrafter chaining, you can now only configure the locking mode on the head of the chain.
-   For now, it is not possible to rotate the Relay. This behavior will be restored later.

### Fixed

-   Fixed cables broken with a Wrench not stacking with any other cables.
-   Fixed colored importers, exporters, and external storages not dropping when broken.
-   Fixed fluid amounts displaying to 0.1 buckets and not 0.001. The suffix "m" is used.
-   Fixed Pattern Grid processing matrix input slots not mentioning "SHIFT click to clear".
-   Fixed amount buttons not working in the Autocrafting Preview when the amount is "0" after clicking the "Max" button.
-   Fixed not being able to insert resources in a Grid when clicking on something that is autocraftable.
-   Fixed not being able to specify amount &lt; 1 in filter and pattern creation slots.
-   Fixed visual bug where fluid containers were being filled when hovering over a fluid in the Grid.
-   Fixed clear button in the Pattern Grid for the smithing table not working.
-   Fixed item count rendering behind the item in the Interface slots.
-   Fixed pickaxes not influencing break speed of blocks.
-   Fixed non-autocraftables Grid view filter still showing autocraftable resources as they get inserted.
-   Fixed filter items from the Pattern Grid crafting matrix being dropped when the Pattern Grid is broken.
-   Fixed created pattern not being insertable in an Autocrafter when it is dropped after breaking the Pattern Grid.
-   Fixed autocrafting tasks being completed when not all processing outputs have been received yet.
-   Fixed items not stacking when externally interacting with an Interface.
-   Fixed Constructor in default scheduling mode not trying other filters when resource is not available.
-   Fixed autocrafter chaining returning "Machine not found" when starting autocrafting.
-   Fixed Regulator Upgrade not being allowed 4 times in the Importer or Exporter.

## [2.0.0-milestone.4.13] - 2025-02-01

### Added

-   Colored variants are now moved to a separate creative mode tab.
-   You can now start autocrafting tasks in the Relay's output network, using patterns and autocrafters from the input network.

### Fixed

-   Fixed Crafter and Security Manager bottom sides being lit on NeoForge.
-   Fixed pattern input slot in the Pattern Grid not being accessible as an external inventory.
-   Fixed duplication bug with the Crafting Grid matrix and insert-only storages.
-   Fixed not being able to deselect the Grid search box.
-   Fixed Storage Block not showing the amount and capacity when inactive.

## [2.0.0-milestone.4.12] - 2025-01-27

### Added

-   Autocrafting engine.
-   The crafting preview now has the ability to fill out the maximum amount of a resource you can currently craft.
-   In the crafting preview, you can now indicate whether you want to be notified when an autocrafting task is finished.

### Changed

-   Autocrafting now handles multiple patterns with the same output correctly by trying to use the pattern with the
    highest priority first. If there are missing resources, lower priority patterns are checked.
-   The Autocrafter now faces the block you're clicking when placing it, like the other cable blocks (like the Exporter or Importer).
-   You can no longer cancel autocrafting tasks if there is not enough space in storage to return the intermediate task storage.
-   The Autocrafting Monitor now shows the machine in which a resource is processing.

### Fixed

-   Fixed amount in amount screens resetting when resizing the screen.

## [2.0.0-milestone.4.11] - 2024-12-08

### Added

-   Ability to differentiate between insert and extract storage priorities. By default, the extract priority will match the insert priority unless configured otherwise.

### Fixed

-   Fixed External Storage not connecting properly to fluid storages.
-   Fixed Interface filter not respecting maximum stack size of a resource.
-   Fixed potential crash when trying to build cable shapes.
-   Fixed storage disk upgrade recipes not showing properly in recipe viewers.
-   Protect against crashes from other mods when trying to build the cached Grid tooltip.
-   Fixed charging energy items not working on Fabric.

## [2.0.0-milestone.4.10] - 2024-11-24

### Added

-   Autocrafting Monitor
-   Wireless Autocrafting Monitor
-   Creative Wireless Autocrafting Monitor

### Changed

-   The Autocrafting Monitor now has a sidebar with all tasks instead of using tabs.
-   The auto-selected search box mode is now a global option used in the Autocrafter Manager as well.

### Removed

-   Block of Quartz Enriched Iron (has been moved to addon mod)
-   Block of Quartz Enriched Copper (has been moved to addon mod)

## [2.0.0-milestone.4.9] - 2024-11-01

### Added

-   Autocrafter Manager
-   You can now configure the view type of the Autocrafter Manager:
    -   Visible (only show autocrafters that are configured to be visible to the Autocrafter Manager)
    -   Not full (only show autocrafters that are not full yet)
    -   All (show all autocrafters)

### Changed

-   The search field in the Autocrafter Manager can now search in:
    -   Pattern inputs
    -   Pattern outputs
    -   Autocrafter names
    -   All of the above (by default)
-   Due to technical limitations and the new filtering options listed above being client-side only, you can no longer shift-click patterns in the Autocrafter Manager.
-   In the Autocrafter, you can now configure whether it is visible to the Autocrafter Manager (by default it's visible).

## [2.0.0-milestone.4.8] - 2024-10-12

### Added

-   Autocrafter
    -   Note: autocrafting itself hasn't been implemented yet. This is the in-game content, but not the autocrafting engine itself yet. 
-   The Relay now has support for propagating autocrafting when not in pass-through mode.

### Changed

-   The Crafter has been renamed to "Autocrafter".
-   Optimized memory usage and startup time of cable models. After updating, cables will appear disconnected, but this is only visual. Cause a block update to fix this.
-   Optimized performance of searching in the Grid.
-   Custom titles that overflow will now have a marquee effect instead, for every GUI.
-   You can now define a priority in the Autocrafter.
-   You can now change the name of a Autocrafter in the GUI.
-   Changed "Crafter mode" to "Locking mode" with following options:
    -   Never
    -   Lock until redstone pulse is received
    -   Lock until connected machine is empty (new, facilitates easier "blocking mode" without redstone)
    -   Lock until all outputs are received (new, facilitates easier "blocking mode" without redstone)
    -   Lock until low redstone signal
    -   Lock until high redstone signal
-   Resources in the Grid that are autocraftable now display an orange backdrop and tooltip to indicate whether the resource is autocraftable at a glance.
-   Slots used in the Pattern Grid for pattern encoding and Crafting Grid crafting matrix slots now display an orange backdrop and tooltip to indicate whether the item is autocraftable at a glance. This checks patterns from your network and from your inventory.
-   Added help tooltip for filtering based on recipe items in the Crafting Grid.
-   The crafting amount and crafting preview screens have been merged. Changing the amount will update the live preview.
-   The numbers on the crafting preview screen are now compacted with units.
-   When requesting autocrafting multiple resources at once, which can happen via a recipe mod, all the crafting requests are now listed on the side of the GUI.
-   You can now request autocrafting from the Storage Monitor if the resource count reaches zero.

### Fixed

-   Fixed mouse keybindings not working on NeoForge.
-   Fixed upgrade destinations not being shown on upgrades.
-   Fixed resources with changed data format or ID causing entire storage to fail to load.
-   Fixed crash when trying to export fluids from an External Storage on Fabric.
-   The Configuration Card can now also transfer the (configured) Regulator Upgrade.

## [2.0.0-milestone.4.7] - 2024-08-11

### Added

-   You can now upgrade Storage Disks and Storage Blocks to a higher tier by combining with a higher tier Storage Part. The original Storage Part will be returned.

### Changed

-   Updated to Minecraft 1.21.1.
-   The Network Transmitter and Wireless Transmitter GUI now has an inactive and active GUI animation.
-   The Wireless Transmitter now shows whether it's inactive in GUI instead of always showing the range.

### Fixed

-   Use new slimeballs convention tag for Processor Binding.
-   Portable Grid search bar texture being positioned in the wrong way.
-   External Storage screen unnecessarily showing upgrade slots.
-   Grid setting changes not persisting after restarting Minecraft.
-   Fixed not being able to extract fluids from the Grid with an empty bucket or other empty fluid container.
-   All blocks and items now correctly retain their custom name.

## [2.0.0-milestone.4.6] - 2024-08-08

### Added

-   Pattern Grid
-   Pattern

### Changed

-   The Pattern now shows the recipe in the tooltip.
-   When a Pattern is created for a recipe, the Pattern will have a different texture and name to differentiate between empty patterns.
-   The Pattern Grid now has additional support for encoding stonecutter and smithing table recipes.
-   The Pattern output is now always rendered in the Pattern Grid result slot.
-   You can now search in the Pattern Grid alternatives screen.
-   In the Pattern Grid alternatives screen, all resources belonging to a tag or no longer shown at once. You can expand or collapse them.
-   The tag names in the Pattern Grid alternatives screen will now be translated.
-   "Exact mode" in the Pattern Grid has been replaced with "Fuzzy mode" (inverse).

### Fixed

-   Clicking on a scrollbar no longer makes a clicking sound.
-   Incorrect and outdated (mentioning NBT tags) help explanations for fuzzy mode.
-   Amount screen allowing more than the maximum for fluids.
-   Potential text overflow in the Grid for localization with long "Grid" text.

## [2.0.0-milestone.4.5] - 2024-07-26

### Added

-   Ability to extract fluids from the Interface using an empty bucket or other empty fluid container.
-   Support for the NeoForge config screen.

### Fixed

-   Fixed crash when trying to export fluids into an Interface on Fabric.
-   Fixed Relay configuration not being correct on NeoForge.
-   Fixed crash in logs when trying to quick craft an empty result slot in the Crafting Grid.
-   Fixed recipes not using silicon tag and Refined Storage silicon not being tagged properly.

## [2.0.0-milestone.4.4] - 2024-07-10

## [2.0.0-milestone.4.3] - 2024-07-06

### Added

-   Ability to open Portable Grid with a keybinding.

### Fixed

-   Fixed Relay model not being able to load correctly.
-   Fixed not being able to ghost drag resources from recipe viewers into filter slots on NeoForge.
-   Fixed extra dark backgrounds due to drawing background on GUIs twice.
-   Fixed Configuration Card not being able to transfer upgrades for the Wireless Transmitter.
-   Fixed upgrade inventories not maintaining order after reloading. Upgrade inventories from the milestone 4.2 are
    incompatible and will be empty.
-   Fixed Wireless Transmitter not dropping upgrades when breaking block.

## [2.0.0-milestone.4.2] - 2024-07-06

## [2.0.0-milestone.4.1] - 2024-07-05

### Fixed

-   Fixed creative mode tab icon on NeoForge showing a durability bar.

## [2.0.0-milestone.4.0] - 2024-07-04

### Added

-   Ported to Minecraft 1.21.
-   More help information for items.
-   Quartz Enriched Copper, used to craft cables.
-   Block of Quartz Enriched Copper

### Changed

-   The mod ID has been changed from "refinedstorage2" to "refinedstorage". Worlds that used milestone 3 on Minecraft
    1.20.4 are no longer compatible.
-   Recipes now use common tag conventions from NeoForge and Fabric.

### Fixed

-   Regulator Upgrade having wrong GUI title.
-   Crafting Grid not dropping crafting matrix contents when broken.
-   "+1" button on amount screen not doing anything.

## [2.0.0-milestone.3.14] - 2024-06-28

### Added

-   Disk Interface (formerly known as the "Disk Manipulator").
-   Item tag translations.

### Fixed

-   Relay having no help tooltip.
-   Fixed bug where adding more Speed Upgrades would actually slow down the device even more.
-   Fixed missing textures for scheduling mode side button.

## [2.0.0-milestone.3.13] - 2024-06-16

## [2.0.0-milestone.3.12] - 2024-06-16

### Removed

-   The Trinkets integration has been removed and will be moved to an addon mod.

## [2.0.0-milestone.3.11] - 2024-06-16

### Removed

-   The Curios integration has been removed and will be moved to an addon mod.

## [2.0.0-milestone.3.10] - 2024-06-16

## [2.0.0-milestone.3.9] - 2024-06-09

### Fixed

-   Side button tooltip rendering issue with ModernUI.

## [2.0.0-milestone.3.8] - 2024-06-08

### Removed

-   The REI integration has been removed and will be moved to an addon mod.

## [2.0.0-milestone.3.7] - 2024-06-03

### Removed

-   The JEI integration has been removed and will be moved to an addon mod.

## [2.0.0-milestone.3.6] - 2024-05-18

### Added

-   Relay

### Changed

-   The Detector, Network Receiver, Network Transmitter and Security Manager will now always connect regardless of color.
-   The Relay now has a "pass-through" mode. By default, pass-through is on, which means that when the Relay is active,
    the network signal from the input network will be passed through as-is to the output side.
-   When the "pass-through" mode on the Relay is off, the network signal from the input network will no longer be passed
    through as-is to the output side, but you can choose to pass the energy buffer, security settings or (specific)
    storage resources of the input network to the output network.
-   When using the Relay when "pass-through" mode is off, and when passing all storage resources or specific storage
    resources, you can choose the filter mode, whether fuzzy mode is enabled, the access mode and the priority of the
    storage exposed to the output network.

### Fixed

-   Double slot highlighting in the Grid.
-   Improved data corruption protection for storages.

## [2.0.0-milestone.3.5] - 2024-04-04

### Added

-   Security Card
-   Fallback Security Card
-   Security Manager

### Changed

-   The permissions for a Security Card must be configured through the card itself, instead of via the Security Manager.
-   The Security Card can be bound to other (currently online) players via its GUI.
-   The binding of a Security Card can now be cleared.
-   The Security Card tooltip and GUI now show whether the permission has been touched/changed in any way.
-   As soon as a Security Manager is placed, the storage network will be locked down by default. Start adding Security
    Cards to allow or deny specific access to players.
-   To not lock the entire network by default for players who do not have a matching Security Card, a Fallback Security
    Card can be used to configure this behavior.
-   Smooth scrolling, screen size and max row stretch are no longer Grid-specific settings, but are now global settings.

### Fixed

-   Wireless Grid name not being correct in the GUI.

## [2.0.0-milestone.3.4] - 2024-03-16

### Added

-   Void excess mode to storages.

### Fixed

-   Fixed losing disk when using Wrench dismantling on the Portable Grid.
-   Fixed losing energy when using Wrench dismantling on the Portable Grid and the Controller.
-   Fixed changing side buttons not working on Forge.
-   Fixed External Storage not displaying empty allowlist warning.
-   Fixed incrementing starting from 1 in amount screens not having an intended off-by-one.
-   Fixed problems moving network devices with "Carry On" mod.
-   Fixed escape key not working on auto-selected Grid search box.

## [2.0.0-milestone.3.3] - 2024-02-17

### Added

-   Ported to Minecraft 1.20.4.
-   Custom disk models. Fluid disks now have a different model.
-   Portable Grid
-   Chinese translation by [@Jiangsubei](https://github.com/Jiangsubei).

### Changed

-   The Portable Grid now shows an energy bar in the UI.
-   The energy bar on creative items now shows the infinity symbol instead of the whole amount.

### Fixed

-   Fixed bug where Grid contents weren't synced properly when a network merge occurs.
-   Fixed incompatibility crash with InvMove on Fabric.

## [2.0.0-milestone.3.2] - 2023-11-03

### Added

-   Configuration Card. It copies device configurations and can transfer upgrades.
-   Network Receiver
-   Network Card
-   Network Transmitter

### Changed

-   The Network Transmitter now goes into an "errored" state if there is no connection (anymore) with the Network
    Receiver (due to chunk unloading for example).
-   The Network Transmitter will actively try to reconnect with the Network Receiver if connection is lost.

### Fixed

-   Inactive Wireless Transmitter model being emissive.
-   Unneeded network graph updating after placing a network device.
-   Cable blocks not updating connections properly when using wrench.

## [2.0.0-milestone.3.1] - 2023-10-30

### Added

-   "Open Wireless Grid" keybinding.
-   Curios integration on Forge.
-   Trinkets integration on Fabric.
-   Storage Monitor

### Changed

-   You can now recharge the Controller in item form.

### Fixed

-   Fixed a random Grid crash.

### Removed

-   The `useEnergy` config option for the Wireless Grid. If you do not wish to use energy, use the
    Creative Wireless Grid.

## [2.0.0-milestone.3.0] - 2023-08-27

### Added

-   Wireless Grid
-   Creative Wireless Grid
-   Wireless Transmitter
-   Range Upgrade
-   Creative Range Upgrade
-   Fully charged Controller variants to the creative mode tab.

### Changed

-   The Forge variant now targets NeoForge instead of Forge.
-   You can now always open the Wireless Grid, even if there is no network bound or if the Wireless Grid is out of
    energy.

### Fixed

-   Fixed inactive Grid slots still rendering resources.
-   Fixed being able to interact with inactive Grid.
-   Fixed nearly on/off Controller model not being rendered correctly on Forge.
-   Fixed Controller energy tooltip not working.

## [2.0.0-milestone.2.14] - 2023-08-19

### Added

-   Support for JEI/REI exclusion zones.
-   Support for JEI/REI ghost ingredient dragging.

## [2.0.0-milestone.2.13] - 2023-08-18

### Changed

-   The Interface now supports fluids.

### Fixed

-   Fixed filter slot hints not being aware of the resource types that they can show in a slot.
-   Fixed Exporter only exporting 1 mB per cycle on Forge.
-   Fixed not being able to use any blocks on Fabric or Forge.
-   Fixed External Storage crash on Fabric when a stack with zero amount is exposed.

## [2.0.0-milestone.2.12] - 2023-08-06

### Added

-   Constructor
-   Regulator Upgrade
-   Filter slot hints that show which resource will be put in a filter slot and what the effect of the filter is on the
    device.
-   Grid slot hints that show which resource will be inserted or extracted in a Grid.
-   Help information to the side buttons by pressing SHIFT.
-   Help information on items.
-   A warning to the "filter mode" button on the storage screens if there is an allowlist with no configured filters.
-   The "supported by" tooltip on upgrade items now shows the devices that accept the upgrade.

### Changed

-   The Constructor crafting recipe now takes 2 diamonds instead of 2 redstone.
-   You can now select a "Scheduling mode" in the Constructor: first available, round robin, random.
-   The "applicable upgrades" tooltip on the upgrade slot tooltip now shows the upgrade items in item form.
-   The Regulator Upgrade now works in an Importer as well. It will only keep importing until the configured amount is
    reached.
-   The Regulator Upgrade now needs to be configured separately, by using the upgrade. It can no longer be configured in
    the device GUI itself.

### Fixed

-   Fixed Grid voiding fluids if there was no space in inventory on Fabric.
-   Fixed Grid dropping fluid buckets if there was no space in inventory on Forge.
-   Fixed compatibility with custom tooltips in the Grid.
-   Fixed bundle tooltip in the Grid.
-   Fixed changes to access mode or fuzzy mode not being persisted.
-   Fixed being able to put any item in the upgrade slots.

## [2.0.0-milestone.2.11] - 2023-07-04

### Added

-   Ported to Minecraft 1.20.1.

### Fixed

-   Fixed not firing block break event on Fabric for the Destructor.

## [2.0.0-milestone.2.10] - 2023-05-29

### Added

-   Ported to Minecraft 1.19.4
-   Destructor
-   Fortune Upgrade (I, II and III)
-   Silk Touch Upgrade

### Changed

-   The Detector screen now is a proper amount screen by having increment/decrement buttons and scrollbar support.
-   The amount in an amount screen is now colored red if the amount is invalid.
-   The Destructor crafting recipe now takes 2 diamonds instead of 2 redstone.

### Fixed

-   Fixed missing Speed Upgrade energy usage config on Forge.
-   Fixed Grid screen not handling network changes properly.
-   Fixed Grid scrollbar scrolling when using SHIFT or CTRL.
-   Fixed wrong Controller tooltip.

### Removed

-   Removed "Fuzzy mode" from the Destructor as the filter in the Destructor compares with the block anyway.

## [2.0.0-milestone.2.9] - 2023-03-31

### Fixed

-   Fixed not being able to update filter slots on servers.

### Added

-   Detector

### Changed

-   Detectors can now be placed sideways or upside down.
-   Detectors no longer detect all resources when unconfigured.
-   Redstone updates by Detectors are now rate-limited to once per second.
-   For fluids, the Detector now always accepts the amount in buckets.

## [2.0.0-milestone.2.8] - 2023-03-04

### Fixed

-   Fixed Disk Drive having 9 slots instead of 8.
-   Fixed slow world loading.

### Added

-   The upgrade slots now show their supported upgrades.
-   Different Cable colors. They only connect to same colored cables or the default cable.
-   Colored variant of exporters, importers and external storages. They connect the same way as colored cables.
-   Support for using the R/U keys in JEI and REI on Grid slots and filtering slots
-   Crafting Grid.
-   JEI and REI recipe transfer integration for the Crafting Grid.
-   The crafting matrix in the Crafting Grid now has a button and keybinding to clear to the player inventory.
    The keybinding is only available on Forge.
-   A config option to clear items from the Crafting Grid crafting matrix to the player or network inventory.
-   Support for collapsable entries for REI.
-   Pressing CTRL + SHIFT on the crafting result slot filters the Grid view based on the items in the crafting matrix.
    The reason for this is that you can quickly see how much you have left in the storage network.

### Changed

-   The button to clear to the network inventory next to the crafting matrix in the Crafting Grid is now disabled if
    the Crafting Grid is inactive.
-   The keybinding to clear the Crafting Grid matrix to the network inventory is only available on Forge.
-   The JEI recipe transfer integration for the Crafting Grid now only supports regular crafting recipes.
-   Decreased amount of logging to the info level. Now most logging happens on the debug level.

### Removed

-   Removed amount of stacks and max stacks stored on item storage tooltips.

## [2.0.0-milestone.2.7] - 2023-01-31

### Added

-   Added a "Storage channel" filter in the Grid that determines which resource type is shown. Defaults to "All".

### Changed

-   Ported to Minecraft 1.19.3.
-   The regular Grid now shows fluids as well.
-   You can insert fluids in the Grid by right-clicking a fluid container in the Grid slots.
-   You no longer have to explicitly select a resource type for the filter configuration slots. You can set a fluid
    by right-clicking a fluid container in the filter slots.
-   You can no longer insert fluids into the Grid or filter slots straight from the player inventory slots, you have to
    insert the fluid while holding the fluid container.

### Removed

-   Removed the Fluid Grid, which has been combined into the regular Grid.

## [2.0.0-milestone.2.6] - 2023-01-13

### Fixed

-   Fixed missing recoloring recipes for Grid and Controller to default color.
-   Fixed missing recoloring recipes for Fluid Grid.

## [2.0.0-milestone.2.5] - 2023-01-11

### Fixed

-   Fixed IO loops caused by Interfaces stealing from each other.
-   Fixed storages from an External Storage not reporting when a resource has last changed.

### Changed

-   An Interface that is acting as External Storage can no longer extract or insert from other Interfaces (and itself)
    that are acting as External Storage.

## [2.0.0-milestone.2.4] - 2022-11-01

### Fixed

-   Fixed missing AutoConfig config option translations on Fabric.
-   Fixed Grid resource failing to insert if another resource with the same name but different NBT data already exists.
-   Fixed Importer not dropping upgrades when broken.
-   Fixed Disk Drive inventory not being available as external inventory on Forge.

### Added

-   Exporter
-   Interface
-   External Storage

### Changed

-   You can now select a "Scheduling mode" in the Exporter: first available, round robin, random.
-   The Interface no longer has dedicated import slots. The imported items now go into the export slots.
-   The Interface now imports items immediately.
-   "Exact mode" has been replaced with "Fuzzy mode", which is off by default for performance.
-   The External Storage no longer shows the amount of resources stored on the GUI.
-   The External Storage now supports multiple resource types at the same time.
-   The External Storage no longer checks for external changes every tick, but rather has a cooldown system.

## [2.0.0-milestone.2.3] - 2022-08-26

### Changed

-   Ported to Minecraft 1.19.2.

### Fixed

-   Fixed mixin crash on startup on Fabric.

### Added

-   NoIndium mod is now packaged with the mod on Fabric to avoid launching Sodium without Indium.

## [2.0.0-milestone.2.2] - 2022-08-06

### Changed

-   All directional blocks no longer transmit a network signal out of the direction.
-   All directional blocks no longer accept a network signal from the facing direction.
-   Upgrade items now state the applicable destinations in the tooltip.
-   Upgrade items can now have a maximum of 1 type per upgrade inventory.
-   You can now SHIFT + CLICK transfer resources in the filter slots again.

### Fixed

-   Fixed network connection state not rebuilding after using Wrench on a directional block.
-   Fixed Grid tooltip being too small in some cases and item durability not being rendered.

### Added

-   Upgrade
-   Speed Upgrade
-   Stack Upgrade

## [2.0.0-milestone.2.1] - 2022-07-30

### Changed

-   The Importer will now extract as much of 1 resource type as possible, according to the per tick transfer quota, at
    once for all the inventory slots.
-   The Importer no longer transmits a network signal on the direction it's facing.
-   The Importer can now import from the Disk Drive.
-   The Importer no longer has a dedicated item/fluid mode. It will import what it's connected to, 1 resource type per
    tick is possible.
-   Updated to the latest Forge version.
-   Ported to Minecraft 1.19.1.

### Fixed

-   Fixed Grid stack zeroing not working correctly when Auto-selected mode is on.
-   Fixed transferring items into Grid with NBT tag on Forge not working correctly.

### Added

-   Importer.
-   Emissive rendering.

## [2.0.0-milestone.2.0] - 2022-07-05

### Changed

-   Ported to Minecraft 1.19.

### Added

-   Added JEI support to Fabric.
-   Added REI support to Forge.

### Fixed

-   Fixed resource filter container updates not arriving properly on Forge.

## [2.0.0-milestone.1.4] - 2022-06-22

### Added

-   The Wrench now dismantles devices when crouching.
    -   The Disk Drive in item form now supports rendering of disks that were dismantled.
    -   In order to retain Controller energy, the Controller must now be dismantled.
    -   All config and upgrades are transferred to the item.
-   You can now use any Wrench from other mods in order to rotate or dismantle.
-   Item and fluid storage blocks.
-   Initial advancements.

### Fixed

-   Fixed inventory contents of devices not retaining their original order when reloading a world.
-   Fixed bug where (already opened) Grid doesn't update if a storage is removed.
-   Fixed last modified info in the Grid not being persisted.
-   Fixed removals in filter inventory not being saved properly.

### Changed

-   Ported to Minecraft 1.18.2.
-   Grid auto-selection and JEI/REI synchronization are now two different options.
-   Grid display settings are now stored in the client configuration, no longer per-block.
-   You now need to crouch with a dye in order to change the color of a device.
-   Item storage capacities are now multiples of 1024 to make it more stack-size friendly.
-   Storage tooltips now have colors.
-   Storage tooltips now show percentage full.
-   Item storage tooltips now show amount of stacks and max stacks stored.

### Removed

-   Removed the Patchouli integration.

## [2.0.0-milestone.1.3] - 2022-02-12

### Added

-   Forge support.

### Fixed

-   Any block can be rotated now if the item tag matches `c:wrenches`.

## [2.0.0-milestone.1.2] - 2021-12-23

### Added

-   Fluid Storage Part
-   Fluid Storage Disk
-   Fluid Grid
-   Wrench
-   Tooltip search in the Grid with unary operator "#".

### Fixed

-   Fix Disk Drive item filters not being applied when reloading a world.
-   Fix Storage Disk contents being scrambled when other mods are being added or removed.
-   Fix rendering crash with the Disk Drive.
-   Fix colored blocks having incorrect names in WTHIT.
-   Prevent loading unloaded chunks.
-   Fix various bugs related to networks and chunk loading/unloading.
-   Fix not being able to move network devices with mods like Carrier.
-   Fix CTRL + CLICK in creative mode not retaining block data.
-   Fix item quantity not being formatted in the Grid.
-   Fix amount in detailed Grid tooltip being formatted with units.

### Changed

-   Ported to Minecraft 1.18.1.
-   Modularized the codebase.
-   The capacity of the various fluid storage part and fluid storage disk tiers are now described in bucket form, no
    longer in mB form.
-   The Wrench now plays a sound effect when used.
-   The Wrench works on any block that has the `fabric:wrenchables` tag. Other mods can identify the Refined Storage
    wrench by checking the `fabric:wrenches` tag.
-   Made energy usage by the Grid and Disk Drive less power-hungry.
-   The Controller now displays an energy bar on the item.
-   Upgrade Team Reborn Energy API.
-   Made block breaking faster.
-   Refined Storage now uses the bundled AutoConfig with ClothConfig and bundles ClothConfig.
-   Item quantity of "1" is now always being rendered in the Grid.
-   Exact mode is now off by default.

### Removed

-   LibBlockAttributes is no longer used and thus no longer bundled with Refined Storage 2.

## [2.0.0-milestone.1.1] - 2021-08-16

### Added

-   New Grid size: "Extra large" (12 rows)

### Fixed

-   Fix crash when transferring items in the Controller screen.
-   Fix Disk Drive leds not being stable.
-   Fix block variants not being present on Patchouli book entries.

### Changed

-   Ported to Minecraft 1.17.1.
-   Implemented a new networking system.

## [2.0.0-milestone.1.0] - 2021-05-21

### Added

-   Controller
-   Grid
-   Disk Drive
-   Storage Part
-   Storage Disk
-   Storage Housing
-   Construction Core
-   Destruction Core
-   Cable
-   Machine Casing
-   Quartz Enriched Iron
-   Block of Quartz Enriched Iron
-   (Raw) Basic Processor
-   (Raw) Improved Processor
-   (Raw) Advanced Processor
-   Silicon
-   Processor Binding
-   Integration with Roughly Enough Items in the form of a REI search box mode.
-   Integration with ModMenu.
-   Integration with Patchouli.
-   Integration with AutoConfig1u.
-   Integration with ClothConfig.
-   Integration with Team Reborn Energy.

### Changed

-   Re-arranged the Disk Drive GUI slightly. The priority button has been moved to the side.
-   The "Priority" screen now has a reset button.
-   "Whitelist" has been renamed to "Allowlist".
-   "Blacklist" has been renamed to "Blocklist".
-   Contents of storages in "insert-only" mode are now visible in the Grid.
-   The Grid keybindings got changed slightly. Consult the Patchouli documentation.
-   The Grid search bar now has much more powerful searching, supporting expressions. Consult the Patchouli documentation.
-   A single item in storage can now be larger than 2,147,483,647.
-   You can now place directional blocks facing up/down.
-   Emissive rendering isn't implemented yet.
-   You can now add multiple controllers to a network to meet the energy requirements of your network.
-   The Priority screen now has a "Reset" button.
-   The Grid can now use smooth scrolling.
-   The Grid now has syntax highlighting for the search query.

[Unreleased]: https://github.com/refinedmods/refinedstorage2/compare/v3.0.0-beta.1...HEAD

[3.0.0-beta.1]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.1...v3.0.0-beta.1

[2.0.1]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0...v2.0.1

[2.0.0]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.17...v2.0.0

[2.0.0-beta.17]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.16...v2.0.0-beta.17

[2.0.0-beta.16]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.15...v2.0.0-beta.16

[2.0.0-beta.15]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.14...v2.0.0-beta.15

[2.0.0-beta.14]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.13...v2.0.0-beta.14

[2.0.0-beta.13]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.12...v2.0.0-beta.13

[2.0.0-beta.12]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.11...v2.0.0-beta.12

[2.0.0-beta.11]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.10...v2.0.0-beta.11

[2.0.0-beta.10]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.9...v2.0.0-beta.10

[2.0.0-beta.9]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.8...v2.0.0-beta.9

[2.0.0-beta.8]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.7...v2.0.0-beta.8

[2.0.0-beta.7]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.6...v2.0.0-beta.7

[2.0.0-beta.6]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.5...v2.0.0-beta.6

[2.0.0-beta.5]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.4...v2.0.0-beta.5

[2.0.0-beta.4]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.3...v2.0.0-beta.4

[2.0.0-beta.3]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.2...v2.0.0-beta.3

[2.0.0-beta.2]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-beta.1...v2.0.0-beta.2

[2.0.0-beta.1]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.14...v2.0.0-beta.1

[2.0.0-milestone.4.14]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.13...v2.0.0-milestone.4.14

[2.0.0-milestone.4.13]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.12...v2.0.0-milestone.4.13

[2.0.0-milestone.4.12]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.11...v2.0.0-milestone.4.12

[2.0.0-milestone.4.11]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.10...v2.0.0-milestone.4.11

[2.0.0-milestone.4.10]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.9...v2.0.0-milestone.4.10

[2.0.0-milestone.4.9]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.8...v2.0.0-milestone.4.9

[2.0.0-milestone.4.8]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.7...v2.0.0-milestone.4.8

[2.0.0-milestone.4.7]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.6...v2.0.0-milestone.4.7

[2.0.0-milestone.4.6]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.5...v2.0.0-milestone.4.6

[2.0.0-milestone.4.5]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.4...v2.0.0-milestone.4.5

[2.0.0-milestone.4.4]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.3...v2.0.0-milestone.4.4

[2.0.0-milestone.4.3]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.2...v2.0.0-milestone.4.3

[2.0.0-milestone.4.2]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.1...v2.0.0-milestone.4.2

[2.0.0-milestone.4.1]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.4.0...v2.0.0-milestone.4.1

[2.0.0-milestone.4.0]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.14...v2.0.0-milestone.4.0

[2.0.0-milestone.3.14]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.13...v2.0.0-milestone.3.14

[2.0.0-milestone.3.13]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.12...v2.0.0-milestone.3.13

[2.0.0-milestone.3.12]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.11...v2.0.0-milestone.3.12

[2.0.0-milestone.3.11]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.10...v2.0.0-milestone.3.11

[2.0.0-milestone.3.10]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.9...v2.0.0-milestone.3.10

[2.0.0-milestone.3.9]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.8...v2.0.0-milestone.3.9

[2.0.0-milestone.3.8]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.7...v2.0.0-milestone.3.8

[2.0.0-milestone.3.7]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.6...v2.0.0-milestone.3.7

[2.0.0-milestone.3.6]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.5...v2.0.0-milestone.3.6

[2.0.0-milestone.3.5]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.4...v2.0.0-milestone.3.5

[2.0.0-milestone.3.4]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.3...v2.0.0-milestone.3.4

[2.0.0-milestone.3.3]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.2...v2.0.0-milestone.3.3

[2.0.0-milestone.3.2]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.1...v2.0.0-milestone.3.2

[2.0.0-milestone.3.1]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.0...v2.0.0-milestone.3.1

[2.0.0-milestone.3.0]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.14...v2.0.0-milestone.3.0

[2.0.0-milestone.2.14]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.13...v2.0.0-milestone.2.14

[2.0.0-milestone.2.13]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.12...v2.0.0-milestone.2.13

[2.0.0-milestone.2.12]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.11...v2.0.0-milestone.2.12

[2.0.0-milestone.2.11]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.10...v2.0.0-milestone.2.11

[2.0.0-milestone.2.10]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.9...v2.0.0-milestone.2.10

[2.0.0-milestone.2.9]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.8...v2.0.0-milestone.2.9

[2.0.0-milestone.2.8]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.7...v2.0.0-milestone.2.8

[2.0.0-milestone.2.7]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.6...v2.0.0-milestone.2.7

[2.0.0-milestone.2.6]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.5...v2.0.0-milestone.2.6

[2.0.0-milestone.2.5]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.4...v2.0.0-milestone.2.5

[2.0.0-milestone.2.4]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.3...v2.0.0-milestone.2.4

[2.0.0-milestone.2.3]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.2...v2.0.0-milestone.2.3

[2.0.0-milestone.2.2]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.2.1...v2.0.0-milestone.2.2

[2.0.0-milestone.2.1]: https://github.com/refinedmods/refinedstorage2/compare/2.0.0-milestone.2.0...v2.0.0-milestone.2.1

[2.0.0-milestone.2.0]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.1.4...v2.0.0-milestone.2.0

[2.0.0-milestone.1.4]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.1.3...v2.0.0-milestone.1.4

[2.0.0-milestone.1.3]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.1.2...v2.0.0-milestone.1.3

[2.0.0-milestone.1.2]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.1.1...v2.0.0-milestone.1.2

[2.0.0-milestone.1.1]: https://github.com/refinedmods/refinedstorage2/compare/2.0.0-milestone.1.0...v2.0.0-milestone.1.1

[2.0.0-milestone.1.0]: https://github.com/raoulvdberge/refinedstorage2/releases/tag/v2.0.0-milestone.1.0
