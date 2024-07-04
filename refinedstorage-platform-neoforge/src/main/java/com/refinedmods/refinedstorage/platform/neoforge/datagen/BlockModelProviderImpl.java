package com.refinedmods.refinedstorage.platform.neoforge.datagen;

import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class BlockModelProviderImpl extends BlockModelProvider {
    private static final String PARTICLE_TEXTURE = "particle";
    private static final String CUTOUT_TEXTURE = "cutout";
    private static final String BLOCK_PREFIX = "block";

    private static final ResourceLocation EMISSIVE_CUTOUT = createIdentifier("block/emissive_cutout");
    private static final ResourceLocation EMISSIVE_ALL_CUTOUT = createIdentifier("block/emissive_all_cutout");
    private static final ResourceLocation EMISSIVE_NORTH_CUTOUT = createIdentifier("block/emissive_north_cutout");

    private static final ResourceLocation CUTOUT = createIdentifier("block/cutout");
    private static final ResourceLocation ALL_CUTOUT = createIdentifier("block/all_cutout");
    private static final ResourceLocation NORTH_CUTOUT = createIdentifier("block/north_cutout");

    private static final ResourceLocation BOTTOM_TEXTURE = createIdentifier("block/bottom");

    private static final String CUTOUT_NORTH = "cutout_north";
    private static final String CUTOUT_EAST = "cutout_east";
    private static final String CUTOUT_SOUTH = "cutout_south";
    private static final String CUTOUT_WEST = "cutout_west";
    private static final String CUTOUT_UP = "cutout_up";
    private static final String CUTOUT_DOWN = "cutout_down";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String SOUTH = "south";
    private static final String WEST = "west";
    private static final String UP = "up";
    private static final String DOWN = "down";

    public BlockModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerControllers();
        registerRightLeftBackFrontTopModel(Blocks.INSTANCE.getGrid(), "grid", "");
        registerRightLeftBackFrontTopModel(Blocks.INSTANCE.getCraftingGrid(), "crafting_grid", "");
        registerDetectors();
        registerWirelessTransmitters();
        registerNetworkReceivers();
        registerNetworkTransmitters();
        registerSecurityManagers();
        registerRelays();
        registerDiskInterfaces();
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

    private void registerRightLeftBackFrontTopModel(final BlockColorMap<?, ?> blockMap,
                                                    final String name,
                                                    final String modelPrefix) {
        blockMap.forEach((color, id, block) -> {
            final ResourceLocation cutout = createIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/" + color.getName());
            registerRightLeftBackFrontTopModel(name, modelPrefix + color.getName(), cutout, EMISSIVE_NORTH_CUTOUT);
        });
        final ResourceLocation inactiveCutout = createIdentifier(BLOCK_PREFIX + "/" + name + "/cutouts/inactive");
        registerRightLeftBackFrontTopModel(name, "inactive", inactiveCutout, NORTH_CUTOUT);
    }

    private void registerRightLeftBackFrontTopModel(final String name,
                                                    final String variantName,
                                                    final ResourceLocation cutout,
                                                    final ResourceLocation baseModel) {
        final ResourceLocation right = createIdentifier(BLOCK_PREFIX + "/" + name + "/right");
        final ResourceLocation left = createIdentifier(BLOCK_PREFIX + "/" + name + "/left");
        final ResourceLocation back = createIdentifier(BLOCK_PREFIX + "/" + name + "/back");
        final ResourceLocation front = createIdentifier(BLOCK_PREFIX + "/" + name + "/front");
        final ResourceLocation top = createIdentifier(BLOCK_PREFIX + "/" + name + "/top");
        withExistingParent(BLOCK_PREFIX + "/" + name + "/" + variantName, baseModel)
            .texture(PARTICLE_TEXTURE, right)
            .texture(NORTH, front)
            .texture(EAST, right)
            .texture(SOUTH, back)
            .texture(WEST, left)
            .texture(UP, top)
            .texture(DOWN, BOTTOM_TEXTURE)
            .texture(CUTOUT_TEXTURE, cutout);
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

    private void registerSecurityManagers() {
        final ResourceLocation back = createIdentifier("block/security_manager/back");
        final ResourceLocation front = createIdentifier("block/security_manager/front");
        final ResourceLocation left = createIdentifier("block/security_manager/left");
        final ResourceLocation right = createIdentifier("block/security_manager/right");
        final ResourceLocation top = createIdentifier("block/security_manager/top");
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, receiver) -> {
            final ResourceLocation cutoutBack = createIdentifier(
                "block/security_manager/cutouts/back/" + color.getName()
            );
            final ResourceLocation cutoutFront = createIdentifier(
                "block/security_manager/cutouts/front/" + color.getName()
            );
            final ResourceLocation cutoutLeft = createIdentifier(
                "block/security_manager/cutouts/left/" + color.getName()
            );
            final ResourceLocation cutoutRight = createIdentifier(
                "block/security_manager/cutouts/right/" + color.getName()
            );
            final ResourceLocation cutoutTop = createIdentifier(
                "block/security_manager/cutouts/top/" + color.getName()
            );
            withExistingParent("block/security_manager/" + color.getName(), EMISSIVE_CUTOUT)
                .texture(PARTICLE_TEXTURE, back)
                .texture(NORTH, front)
                .texture(EAST, right)
                .texture(SOUTH, back)
                .texture(WEST, left)
                .texture(UP, top)
                .texture(DOWN, BOTTOM_TEXTURE)
                .texture(CUTOUT_NORTH, cutoutFront)
                .texture(CUTOUT_EAST, cutoutRight)
                .texture(CUTOUT_SOUTH, cutoutBack)
                .texture(CUTOUT_WEST, cutoutLeft)
                .texture(CUTOUT_UP, cutoutTop)
                .texture(CUTOUT_DOWN, BOTTOM_TEXTURE);
        });
        final ResourceLocation cutoutBack = createIdentifier("block/security_manager/cutouts/back/inactive");
        final ResourceLocation cutoutFront = createIdentifier("block/security_manager/cutouts/front/inactive");
        final ResourceLocation cutoutLeft = createIdentifier("block/security_manager/cutouts/left/inactive");
        final ResourceLocation cutoutRight = createIdentifier("block/security_manager/cutouts/right/inactive");
        final ResourceLocation cutoutTop = createIdentifier("block/security_manager/cutouts/top/inactive");
        withExistingParent("block/security_manager/inactive", CUTOUT)
            .texture(PARTICLE_TEXTURE, back)
            .texture(NORTH, front)
            .texture(EAST, right)
            .texture(SOUTH, back)
            .texture(WEST, left)
            .texture(UP, top)
            .texture(DOWN, BOTTOM_TEXTURE)
            .texture(CUTOUT_NORTH, cutoutFront)
            .texture(CUTOUT_EAST, cutoutRight)
            .texture(CUTOUT_SOUTH, cutoutBack)
            .texture(CUTOUT_WEST, cutoutLeft)
            .texture(CUTOUT_UP, cutoutTop)
            .texture(CUTOUT_DOWN, BOTTOM_TEXTURE);
    }

    private void registerRelays() {
        final ResourceLocation in = createIdentifier("block/relay/in");
        final ResourceLocation out = createIdentifier("block/relay/out");
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, receiver) -> {
            final ResourceLocation cutoutIn = createIdentifier("block/relay/cutouts/in/" + color.getName());
            final ResourceLocation cutoutOut = createIdentifier("block/relay/cutouts/out/" + color.getName());
            withExistingParent("block/relay/" + color.getName(), EMISSIVE_CUTOUT)
                .texture(PARTICLE_TEXTURE, in)
                .texture(NORTH, out)
                .texture(EAST, in)
                .texture(SOUTH, in)
                .texture(WEST, in)
                .texture(UP, in)
                .texture(DOWN, in)
                .texture(CUTOUT_NORTH, cutoutOut)
                .texture(CUTOUT_EAST, cutoutIn)
                .texture(CUTOUT_SOUTH, cutoutIn)
                .texture(CUTOUT_WEST, cutoutIn)
                .texture(CUTOUT_UP, cutoutIn)
                .texture(CUTOUT_DOWN, cutoutIn);
        });
        final ResourceLocation cutoutIn = createIdentifier("block/relay/cutouts/in/inactive");
        final ResourceLocation cutoutOut = createIdentifier("block/relay/cutouts/out/inactive");
        withExistingParent("block/relay/inactive", CUTOUT)
            .texture(PARTICLE_TEXTURE, in)
            .texture(NORTH, out)
            .texture(EAST, in)
            .texture(SOUTH, in)
            .texture(WEST, in)
            .texture(UP, in)
            .texture(DOWN, in)
            .texture(CUTOUT_NORTH, cutoutOut)
            .texture(CUTOUT_EAST, cutoutIn)
            .texture(CUTOUT_SOUTH, cutoutIn)
            .texture(CUTOUT_WEST, cutoutIn)
            .texture(CUTOUT_UP, cutoutIn)
            .texture(CUTOUT_DOWN, cutoutIn);
    }

    private void registerDiskInterfaces() {
        registerRightLeftBackFrontTopModel(Blocks.INSTANCE.getDiskInterface(), "disk_interface", "base_");
        Blocks.INSTANCE.getDiskInterface()
            .forEach((color, id, block) -> getBuilder("block/disk_interface/" + color.getName())
                .customLoader((blockModelBuilder, existingFileHelper) -> new CustomLoaderBuilder<>(
                    id,
                    blockModelBuilder,
                    existingFileHelper,
                    true
                ) {
                }).end());
    }
}
