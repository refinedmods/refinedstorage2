# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0-milestone.2.5] - 2023-01-10

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

Device inventories and disks created in v2.0.0-milestone.1.4 will be lost.

### Changed

-   Ported to Minecraft 1.19.

### Added

-   Added JEI support to Fabric.
-   Added REI support to Forge.

### Fixed

-   Fixed resource filter container updates not arriving properly on Forge.

## [2.0.0-milestone.1.4] - 2022-06-22

All device inventories (most notably the Disk Drive inventory) will be empty after upgrading. Make sure to move all
disks, etc. to intermediate storage like a chest.

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

Storage Disks from the previous version are no longer valid. Please move all your items into another storage medium
before updating.

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

Currently, this version only works on Fabric.

This is milestone release, this release could be very unstable, could break worlds and might not be compatible when
upgrading to a later version.

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

[Unreleased]: https://github.com/refinedmods/refinedstorage2/compare/2.0.0-milestone.2.5...HEAD

[2.0.0-milestone.2.5]: https://github.com/refinedmods/refinedstorage2/compare/2.0.0-milestone.2.4...2.0.0-milestone.2.5
