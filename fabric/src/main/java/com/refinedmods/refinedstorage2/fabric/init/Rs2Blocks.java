package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import com.refinedmods.refinedstorage2.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.fabric.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.fabric.block.GridBlock;
import com.refinedmods.refinedstorage2.fabric.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.fabric.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.fabric.block.RelayBlock;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;

public class Rs2Blocks {
    private static final FabricBlockSettings STONE_SETTINGS = FabricBlockSettings
            .of(Material.STONE)
            .hardness(1.9F)
            .resistance(1.9F)
            .sounds(BlockSoundGroup.STONE);

    private final BlockColorMap<GridBlock> grid = new BlockColorMap<>();
    private CableBlock cable;
    private QuartzEnrichedIronBlock quartzEnrichedIron;
    private DiskDriveBlock diskDrive;
    private MachineCasingBlock machineCasing;
    private RelayBlock relay;
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>();

    public void register() {
        cable = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("cable"), new CableBlock());
        quartzEnrichedIron = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("quartz_enriched_iron_block"), new QuartzEnrichedIronBlock(STONE_SETTINGS));
        diskDrive = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("disk_drive"), new DiskDriveBlock(STONE_SETTINGS));
        machineCasing = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("machine_casing"), new MachineCasingBlock(STONE_SETTINGS));
        relay = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("relay"), new RelayBlock());
        grid.putAll((color, nameFactory) -> Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier(nameFactory.apply("grid")), new GridBlock(STONE_SETTINGS)));
        controller.putAll((color, nameFactory) -> Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier(nameFactory.apply("controller")), new ControllerBlock(STONE_SETTINGS, ControllerType.NORMAL)));
        creativeController.putAll((color, nameFactory) -> Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier(nameFactory.apply("creative_controller")), new ControllerBlock(STONE_SETTINGS, ControllerType.CREATIVE)));
    }

    public CableBlock getCable() {
        return cable;
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIron() {
        return quartzEnrichedIron;
    }

    public DiskDriveBlock getDiskDrive() {
        return diskDrive;
    }

    public MachineCasingBlock getMachineCasing() {
        return machineCasing;
    }

    public BlockColorMap<GridBlock> getGrid() {
        return grid;
    }

    public BlockColorMap<ControllerBlock> getController() {
        return controller;
    }

    public RelayBlock getRelay() {
        return relay;
    }

    public BlockColorMap<ControllerBlock> getCreativeController() {
        return creativeController;
    }
}
