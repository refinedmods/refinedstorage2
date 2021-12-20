package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.FeatureFlag;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.fabric.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.platform.fabric.block.RelayBlock;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class Rs2Blocks {
    public static final String BLOCK_TRANSLATION_CATEGORY = "block";

    private static final BlockBehaviour.Properties STONE_SETTINGS = FabricBlockSettings
            .of(Material.STONE)
            .strength(0.5F, 6.0F)
            .sound(SoundType.STONE);

    private final BlockColorMap<ItemGridBlock> grid = new BlockColorMap<>();
    private final BlockColorMap<FluidGridBlock> fluidGrid = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>();
    private CableBlock cable;
    private QuartzEnrichedIronBlock quartzEnrichedIron;
    private DiskDriveBlock diskDrive;
    private MachineCasingBlock machineCasing;
    private RelayBlock relay;

    public void register() {
        cable = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("cable"), new CableBlock());
        quartzEnrichedIron = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("quartz_enriched_iron_block"), new QuartzEnrichedIronBlock(STONE_SETTINGS));
        diskDrive = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("disk_drive"), new DiskDriveBlock(STONE_SETTINGS));
        machineCasing = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("machine_casing"), new MachineCasingBlock(STONE_SETTINGS));

        if (Rs2Mod.FEATURES.contains(FeatureFlag.RELAY)) {
            relay = Registry.register(Registry.BLOCK, Rs2Mod.createIdentifier("relay"), new RelayBlock());
        }

        grid.putAll(color -> Registry.register(
                Registry.BLOCK,
                grid.getId(color, "grid"),
                new ItemGridBlock(
                        STONE_SETTINGS,
                        grid.getName(color, Rs2Mod.createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid"))
                )
        ));
        fluidGrid.putAll(color -> Registry.register(
                Registry.BLOCK,
                fluidGrid.getId(color, "fluid_grid"),
                new FluidGridBlock(
                        STONE_SETTINGS,
                        fluidGrid.getName(color, Rs2Mod.createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid"))
                )
        ));
        controller.putAll(color -> Registry.register(
                Registry.BLOCK,
                controller.getId(color, "controller"),
                new ControllerBlock(
                        STONE_SETTINGS,
                        ControllerType.NORMAL,
                        controller.getName(color, Rs2Mod.createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller"))
                )
        ));
        creativeController.putAll(color -> Registry.register(
                Registry.BLOCK,
                creativeController.getId(color, "creative_controller"),
                new ControllerBlock(
                        STONE_SETTINGS,
                        ControllerType.CREATIVE,
                        creativeController.getName(color, Rs2Mod.createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller"))
                )
        ));
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

    public BlockColorMap<ItemGridBlock> getGrid() {
        return grid;
    }

    public BlockColorMap<FluidGridBlock> getFluidGrid() {
        return fluidGrid;
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
