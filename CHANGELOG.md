# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

-   Configuration Card. It copies device configurations and can transfer upgrades.
-   Network Receiver
-   Network Card
-   Network Transmitter

### Changed

-   The Network Transmitter now goes into an "errored" state if there is no connection (anymore) with the Network Receiver (due to chunk unloading for example).
-   The Network Transmitter will actively try to reconnect with the Network Receiver if connection is lost.

### Fixed

-   Inactive Wireless Transmitter model being emissive.
-   Unneeded network graph updating after placing a network device.

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
-   Filter slot hints that show which resource will be put in a filter slot and what the effect of the filter is on the device.
-   Grid slot hints that show which resource will be inserted or extracted in a Grid.
-   Help information to the side buttons by pressing SHIFT.
-   Help information on items.
-   A warning to the "filter mode" button on the storage screens if there is an allowlist with no configured filters.
-   The "supported by" tooltip on upgrade items now shows the devices that accept the upgrade.

### Changed

-   The Constructor crafting recipe now takes 2 diamonds instead of 2 redstone.
-   You can now select a "Scheduling mode" in the Constructor: first available, round robin, random.
-   The "applicable upgrades" tooltip on the upgrade slot tooltip now shows the upgrade items in item form.
-   The Regulator Upgrade now works in an Importer as well. It will only keep importing until the configured amount is reached.
-   The Regulator Upgrade now needs to be configured separately, by using the upgrade. It can no longer be configured in the device GUI itself.

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
    - The Disk Drive in item form now supports rendering of disks that were dismantled.
    - In order to retain Controller energy, the Controller must now be dismantled.
    - All config and upgrades are transferred to the item.
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

[Unreleased]: https://github.com/refinedmods/refinedstorage2/compare/v2.0.0-milestone.3.1...HEAD

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
