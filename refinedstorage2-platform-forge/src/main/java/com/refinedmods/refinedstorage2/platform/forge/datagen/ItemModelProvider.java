package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.grid.CraftingGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.grid.GridBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ColorMap;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerExporter();
        registerController();
        registerCreativeController();
        registerGrid();
        registerCraftingGrid();
    }

    private void registerCables() {
        final ResourceLocation base = createIdentifier("item/cable/base");
        final ColorMap<CableBlock> cables = Blocks.INSTANCE.getCable();
        cables.forEach((color, cable) -> singleTexture(
            cables.getId(color, createIdentifier("cable")).getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName()))
        );
    }

    private void registerExporter() {
        final ResourceLocation base = createIdentifier("item/exporter/base");
        final ColorMap<ExporterBlock> exporters = Blocks.INSTANCE.getExporter();
        exporters.forEach((color, exporter) -> singleTexture(
            exporters.getId(color, createIdentifier("exporter")).getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerController() {
        final ResourceLocation base = new ResourceLocation("item/generated");
        final ResourceLocation off = createIdentifier("block/controller/off");
        final ResourceLocation nearlyOff = createIdentifier("block/controller/nearly_off");
        final ResourceLocation nearlyOn = createIdentifier("block/controller/nearly_on");
        final ResourceLocation stored = createIdentifier("stored_in_controller");
        final ColorMap<ControllerBlock> controllers = Blocks.INSTANCE.getController();
        controllers.forEach((color, controller) ->
            withExistingParent(controllers.getId(color, createIdentifier("controller")).getPath(), base)
                .override()
                .predicate(stored, 0)
                .model(modelFile(off))
                .end()
                .override()
                .predicate(stored, 0.01f)
                .model(modelFile(nearlyOff))
                .end()
                .override()
                .predicate(stored, 0.3f)
                .model(modelFile(nearlyOn))
                .end()
                .override()
                .predicate(stored, 0.4f)
                .model(modelFile(createIdentifier("block/controller/" + color.getName())))
                .end()
        );
    }

    private void registerCreativeController() {
        final BlockColorMap<ControllerBlock> creativeControllers = Blocks.INSTANCE.getCreativeController();
        creativeControllers.forEach((color, controller) -> withExistingParent(
            creativeControllers.getId(color, createIdentifier("creative_controller")).getPath(),
            createIdentifier("block/controller/" + color.getName())
        ));
    }

    private void registerGrid() {
        final BlockColorMap<GridBlock> grids = Blocks.INSTANCE.getGrid();
        grids.forEach((color, grid) -> withExistingParent(
            grids.getId(color, createIdentifier("grid")).getPath(),
            createIdentifier("block/grid/" + color.getName())
        ));
    }

    private void registerCraftingGrid() {
        final BlockColorMap<CraftingGridBlock> grids = Blocks.INSTANCE.getCraftingGrid();
        grids.forEach((color, grid) -> withExistingParent(
            grids.getId(color, createIdentifier("crafting_grid")).getPath(),
            createIdentifier("block/crafting_grid/" + color.getName())
        ));
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, existingFileHelper);
    }
}
