package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class BlockModelProvider extends net.minecraftforge.client.model.generators.BlockModelProvider {
    public BlockModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerController();
        registerGrid();
    }

    private void registerCables() {
        final ResourceLocation coreBase = createIdentifier("block/cable/core/base");
        final ResourceLocation extensionBase = createIdentifier("block/cable/extension/base");
        Blocks.INSTANCE.getCable().forEach((color, cable) -> {
            final ResourceLocation texture = createIdentifier("block/cable/" + color.getName());
            withExistingParent("block/cable/core/" + color.getName(), coreBase)
                .texture("cable", texture)
                .texture("particle", texture);
            withExistingParent("block/cable/extension/" + color.getName(), extensionBase)
                .texture("cable", texture)
                .texture("particle", texture);
        });
    }

    private void registerController() {
        final ResourceLocation base = createIdentifier("block/emissive_all_cutout");
        final ResourceLocation off = createIdentifier("block/controller/off");
        final ResourceLocation on = createIdentifier("block/controller/on");
        Blocks.INSTANCE.getController().forEach((color, controller) -> {
            final ResourceLocation cutout = createIdentifier("block/controller/cutouts/" + color.getName());
            withExistingParent("block/controller/" + color.getName(), base)
                    .texture("particle", off)
                    .texture("all", on)
                    .texture("cutout", cutout);
        });
    }

    private void registerGrid() {
        Blocks.INSTANCE.getGrid().forEach((color, controller) -> {
            final ResourceLocation cutout = createIdentifier("block/grid/cutouts/" + color.getName());
            registerGrid(color.getName(), cutout);
        });
        final ResourceLocation cutout = createIdentifier("block/grid/cutouts/inactive");
        registerGrid("inactive", cutout, createIdentifier("block/north_cutout"));
    }

    private void registerGrid(final String name, final ResourceLocation cutout) {
        registerGrid(name, cutout, createIdentifier("block/emissive_north_cutout"));
    }

    private void registerGrid(final String name, final ResourceLocation cutout, final ResourceLocation base) {
        final ResourceLocation right = createIdentifier("block/grid/right");
        final ResourceLocation left = createIdentifier("block/grid/left");
        final ResourceLocation back = createIdentifier("block/grid/back");
        final ResourceLocation front = createIdentifier("block/grid/front");
        final ResourceLocation top = createIdentifier("block/grid/top");
        final ResourceLocation bottom = createIdentifier("block/bottom");
        withExistingParent("block/grid/" + name, base)
            .texture("particle", right)
            .texture("north", front)
            .texture("east", right)
            .texture("south", back)
            .texture("west", left)
            .texture("up", top)
            .texture("down", bottom)
            .texture("cutout", cutout);
    }
}
