package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridBlock;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class BlockModelProviderImpl extends BlockModelProvider {
    private static final String PARTICLE_TEXTURE = "particle";
    private static final String CUTOUT_TEXTURE = "cutout";
    private static final String BLOCK_PREFIX = "block";

    private static final ResourceLocation EMISSIVE_ALL_CUTOUT = createIdentifier("block/emissive_all_cutout");
    private static final ResourceLocation EMISSIVE_NORTH_CUTOUT = createIdentifier("block/emissive_north_cutout");
    private static final ResourceLocation NORTH_CUTOUT = createIdentifier("block/north_cutout");
    private static final ResourceLocation ALL_CUTOUT = createIdentifier("block/all_cutout");

    public BlockModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerControllers();
        registerGrids(Blocks.INSTANCE.getGrid(), "grid");
        registerGrids(Blocks.INSTANCE.getCraftingGrid(), "crafting_grid");
        registerDetectors();
        registerWirelessTransmitters();
        registerNetworkReceivers();
        registerNetworkTransmitters();
    }

    private void registerCables() {
        final ResourceLocation coreBase = createIdentifier("block/cable/core/base");
        final ResourceLocation extensionBase = createIdentifier("block/cable/extension/base");
        Blocks.INSTANCE.getCable().forEach((color, id, cable) -> {
            final ResourceLocation texture = createIdentifier("block/cable/" + color.getName());
            withExistingParent("block/cable/core/" + color.getName(), coreBase)
                .texture("cable", texture)
                .texture(PARTICLE_TEXTURE, texture);
            withExistingParent("block/cable/extension/" + color.getName(), extensionBase)
                .texture("cable", texture)
                .texture(PARTICLE_TEXTURE, texture);
        });
    }

    private void registerControllers() {
        final ResourceLocation off = createIdentifier("block/controller/off");
        final ResourceLocation on = createIdentifier("block/controller/on");
        Blocks.INSTANCE.getController().forEach((color, id, controller) -> {
            final ResourceLocation cutout = createIdentifier("block/controller/cutouts/" + color.getName());
            withExistingParent("block/controller/" + color.getName(), EMISSIVE_ALL_CUTOUT)
                .texture(PARTICLE_TEXTURE, off)
                .texture("all", on)
                .texture(CUTOUT_TEXTURE, cutout);
        });
    }

    private void registerGrids(final BlockColorMap<? extends AbstractGridBlock<?, ?>, ?> blockMap, final String name) {
        blockMap.forEach((color, id, block) -> {
            final ResourceLocation cutout = createIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/" + color.getName());
            registerEmissiveGrids(name, color.getName(), cutout);
        });
        final ResourceLocation inactiveCutout = createIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/inactive");
        registerGrids(name, "inactive", inactiveCutout, NORTH_CUTOUT);
    }

    private void registerGrids(final String name,
                               final String variantName,
                               final ResourceLocation cutout,
                               final ResourceLocation baseModel) {
        final ResourceLocation right = createIdentifier(BLOCK_PREFIX + "/" + name + "/right");
        final ResourceLocation left = createIdentifier(BLOCK_PREFIX + "/" + name + "/left");
        final ResourceLocation back = createIdentifier(BLOCK_PREFIX + "/" + name + "/back");
        final ResourceLocation front = createIdentifier(BLOCK_PREFIX + "/" + name + "/front");
        final ResourceLocation top = createIdentifier(BLOCK_PREFIX + "/" + name + "/top");
        final ResourceLocation bottom = createIdentifier("block/bottom");
        withExistingParent(BLOCK_PREFIX + "/" + name + "/" + variantName, baseModel)
            .texture(PARTICLE_TEXTURE, right)
            .texture("north", front)
            .texture("east", right)
            .texture("south", back)
            .texture("west", left)
            .texture("up", top)
            .texture("down", bottom)
            .texture(CUTOUT_TEXTURE, cutout);
    }

    private void registerEmissiveGrids(final String name, final String variantName, final ResourceLocation cutout) {
        registerGrids(name, variantName, cutout, EMISSIVE_NORTH_CUTOUT);
    }

    private void registerDetectors() {
        final ResourceLocation parent = createIdentifier("block/detector/powered");
        final ResourceLocation side = createIdentifier("block/detector/side");
        final ResourceLocation bottom = createIdentifier("block/detector/bottom");
        final ResourceLocation top = createIdentifier("block/detector/top");
        final ResourceLocation particle = createIdentifier("block/detector/side");
        Blocks.INSTANCE.getDetector().forEach((color, id, block) -> {
            final ResourceLocation torch = createIdentifier("block/detector/cutouts/" + color.getName());
            withExistingParent("block/detector/" + color.getName(), parent)
                .texture("side", side)
                .texture("bottom", bottom)
                .texture("top", top)
                .texture(PARTICLE_TEXTURE, particle)
                .texture("torch", torch);
        });
    }

    private void registerWirelessTransmitters() {
        final ResourceLocation parent = createIdentifier("block/wireless_transmitter/active");
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) ->
            withExistingParent("block/wireless_transmitter/" + color.getName(), parent)
                .texture(CUTOUT_TEXTURE, createIdentifier("block/wireless_transmitter/cutouts/" + color.getName())));
    }

    private void registerNetworkReceivers() {
        final ResourceLocation baseTexture = createIdentifier("block/network_receiver/base");
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, receiver) -> {
            final ResourceLocation cutout = createIdentifier("block/network_receiver/cutouts/" + color.getName());
            withExistingParent("block/network_receiver/" + color.getName(), EMISSIVE_ALL_CUTOUT)
                .texture(PARTICLE_TEXTURE, baseTexture)
                .texture("all", baseTexture)
                .texture(CUTOUT_TEXTURE, cutout);
        });
        withExistingParent("block/network_receiver/inactive", ALL_CUTOUT)
            .texture(PARTICLE_TEXTURE, baseTexture)
            .texture("all", baseTexture)
            .texture(CUTOUT_TEXTURE, createIdentifier("block/network_receiver/cutouts/inactive"));
    }

    private void registerNetworkTransmitters() {
        final ResourceLocation baseTexture = createIdentifier("block/network_transmitter/base");
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, receiver) -> {
            final ResourceLocation cutout = createIdentifier("block/network_transmitter/cutouts/" + color.getName());
            withExistingParent("block/network_transmitter/" + color.getName(), EMISSIVE_ALL_CUTOUT)
                .texture(PARTICLE_TEXTURE, baseTexture)
                .texture("all", baseTexture)
                .texture(CUTOUT_TEXTURE, cutout);
        });
        withExistingParent("block/network_transmitter/inactive", ALL_CUTOUT)
            .texture(PARTICLE_TEXTURE, baseTexture)
            .texture("all", baseTexture)
            .texture(CUTOUT_TEXTURE, createIdentifier("block/network_transmitter/cutouts/inactive"));
        withExistingParent("block/network_transmitter/error", EMISSIVE_ALL_CUTOUT)
            .texture(PARTICLE_TEXTURE, baseTexture)
            .texture("all", baseTexture)
            .texture(CUTOUT_TEXTURE, createIdentifier("block/network_transmitter/cutouts/error"));
    }
}
