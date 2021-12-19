# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

Storage Disks from the previous version are no longer valid. Please move all your items into another storage medium
before updating.

### Added

- Fluid Storage Part
- Fluid Storage Disk
- Fluid Grid
- Wrench
- Tooltip search in the Grid with unary operator "#".

### Fixed

- Fix Disk Drive item filters not being applied when reloading a world.
- Fix Storage Disk contents being scrambled when other mods are being added or removed.
- Fix rendering crash with the Disk Drive.

### Changed

- Ported to Minecraft 1.18.1.
- Modularized the codebase.
- The capacity of the various fluid storage part and fluid storage disk tiers are now described in bucket form, no
  longer in mB form.
- The Wrench now plays a sound effect when used.
- The Wrench works on any block that has the `fabric:wrenchables` tag. Other mods can identify the Refined Storage
  wrench by checking the `fabric:wrenches` tag.

### Removed

- LibBlockAttributes is no longer used and thus no longer bundled with Refined Storage 2.

## [2.0.0-milestone.1.1] - 2021-08-16

### Added

- New Grid size: "Extra large" (12 rows)

### Fixed

- Fix crash when transferring items in the Controller screen.
- Fix Disk Drive leds not being stable.
- Fix block variants not being present on Patchouli book entries.

### Changed

- Ported to Minecraft 1.17.1.
- Implemented a new networking system.

## [2.0.0-milestone.1.0] - 2021-05-21

Currently, this version only works on Fabric.

This is milestone release, this release could be very unstable, could break worlds and might not be compatible when
upgrading to a later version.

### Added

- Controller
- Grid
- Disk Drive
- Storage Part
- Storage Disk
- Storage Housing
- Construction Core
- Destruction Core
- Cable
- Machine Casing
- Quartz Enriched Iron
- Block of Quartz Enriched Iron
- (Raw) Basic Processor
- (Raw) Improved Processor
- (Raw) Advanced Processor
- Silicon
- Processor Binding
- Integration with Roughly Enough Items in the form of a REI search box mode.
- Integration with ModMenu.
- Integration with Patchouli.
- Integration with AutoConfig1u.
- Integration with ClothConfig.
- Integration with Team Reborn Energy.

### Changed

- Re-arranged the Disk Drive GUI slightly. The priority button has been moved to the side.
- The "Priority" screen now has a reset button.
- "Whitelist" has been renamed to "Allowlist".
- "Blacklist" has been renamed to "Blocklist".
- Contents of storages in "insert-only" mode are now visible in the Grid.
- The Grid keybindings got changed slightly. Consult the Patchouli documentation.
- The Grid search bar now has much more powerful searching, supporting expressions. Consult the Patchouli documentation.
- A single item in storage can now be larger than 2,147,483,647.
- You can now place directional blocks facing up/down.
- Fullbright rendering isn't implemented yet.
- You can now add multiple controllers to a network to meet the energy requirements of your network.
- The Priority screen now has a "Reset" button.
- The Grid can now use smooth scrolling.
- The Grid now has syntax highlighting for the search query.
