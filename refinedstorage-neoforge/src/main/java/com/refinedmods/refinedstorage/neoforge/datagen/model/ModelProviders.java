package com.refinedmods.refinedstorage.neoforge.datagen.model;

import com.refinedmods.refinedstorage.common.autocrafting.PatternTypeItemModelProperty;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.configurationcard.ActiveConfigurationCardItemModelProperty;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.controller.AbstractControllerBlock;
import com.refinedmods.refinedstorage.common.controller.ControllerEnergyLevelItemModelProperty;
import com.refinedmods.refinedstorage.common.controller.ControllerEnergyType;
import com.refinedmods.refinedstorage.common.detector.DetectorBlock;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlock;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.common.networking.ActiveNetworkCardItemModelProperty;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterState;
import com.refinedmods.refinedstorage.common.security.ActiveSecurityCardItemModelProperty;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.HorizontalDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkBoundItemModelProperty;
import com.refinedmods.refinedstorage.neoforge.networking.ActiveInactiveCablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.networking.CablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.networking.CableUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridUnbakedBlockStateModel;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.data.models.BlockModelGenerators.condition;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

public class ModelProviders extends ModelProvider {
    private static final Identifier BOTTOM_TEXTURE = createIdentifier("block/bottom");

    private static final TextureSlot CUTOUT = TextureSlot.create("cutout");
    private static final TextureSlot NORTH_CUTOUT = TextureSlot.create("cutout_north");
    private static final TextureSlot EAST_CUTOUT = TextureSlot.create("cutout_east");
    private static final TextureSlot SOUTH_CUTOUT = TextureSlot.create("cutout_south");
    private static final TextureSlot WEST_CUTOUT = TextureSlot.create("cutout_west");
    private static final TextureSlot UP_CUTOUT = TextureSlot.create("cutout_up");
    private static final TextureSlot DOWN_CUTOUT = TextureSlot.create("cutout_down");
    private static final TextureSlot CABLE = TextureSlot.create("cable");
    private static final TextureSlot TORCH = TextureSlot.create("torch");

    private static final ModelTemplate EMISSIVE_ALL_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:emissive_all_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.ALL,
        CUTOUT
    );
    private static final ModelTemplate ALL_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:all_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.ALL,
        CUTOUT
    );
    private static final ModelTemplate NORTH_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:north_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        CUTOUT
    );
    private static final ModelTemplate EMISSIVE_NORTH_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:emissive_north_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        CUTOUT
    );
    private static final ModelTemplate EMISSIVE_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:emissive_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        NORTH_CUTOUT,
        EAST_CUTOUT,
        SOUTH_CUTOUT,
        WEST_CUTOUT,
        UP_CUTOUT,
        DOWN_CUTOUT
    );
    private static final ModelTemplate CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        NORTH_CUTOUT,
        EAST_CUTOUT,
        SOUTH_CUTOUT,
        WEST_CUTOUT,
        UP_CUTOUT,
        DOWN_CUTOUT
    );
    private static final ModelTemplate SIDES_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:sides_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        NORTH_CUTOUT,
        EAST_CUTOUT,
        SOUTH_CUTOUT,
        WEST_CUTOUT,
        UP_CUTOUT
    );
    private static final ModelTemplate EMISSIVE_SIDES_CUTOUT_MODEL = ModelTemplates.create(
        "refinedstorage:emissive_sides_cutout",
        TextureSlot.PARTICLE,
        TextureSlot.NORTH,
        TextureSlot.EAST,
        TextureSlot.SOUTH,
        TextureSlot.WEST,
        TextureSlot.UP,
        TextureSlot.DOWN,
        NORTH_CUTOUT,
        EAST_CUTOUT,
        SOUTH_CUTOUT,
        WEST_CUTOUT,
        UP_CUTOUT
    );
    private static final ModelTemplate CABLE_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:cable/base",
        CABLE
    );
    private static final ModelTemplate CABLE_CORE_MODEL = ModelTemplates.create(
        "refinedstorage:cable/core/base",
        CABLE
    );
    private static final ModelTemplate CABLE_EXTENSION_MODEL = ModelTemplates.create(
        "refinedstorage:cable/extension/base",
        CABLE
    );
    private static final TextureSlot FRONT = TextureSlot.create("front");
    private static final ModelTemplate ACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL = ModelTemplates.create(
        "refinedstorage:constructor_destructor/active",
        FRONT,
        CUTOUT
    );
    private static final ModelTemplate INACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL = ModelTemplates.create(
        "refinedstorage:constructor_destructor/inactive",
        FRONT,
        CUTOUT
    );
    private static final ModelTemplate CONSTRUCTOR_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:constructor/base",
        CABLE
    );
    private static final ModelTemplate DESTRUCTOR_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:destructor/base",
        CABLE
    );
    private static final ModelTemplate EXPORTER_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:exporter/base",
        CABLE
    );
    private static final ModelTemplate IMPORTER_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:importer/base",
        CABLE
    );
    private static final ModelTemplate EXTERNAL_STORAGE_ITEM_MODEL = ModelTemplates.createItem(
        "refinedstorage:external_storage/base",
        CABLE
    );
    private static final ModelTemplate ACTIVE_WIRELESS_TRANSMITTER_MODEL = ModelTemplates.create(
        "refinedstorage:wireless_transmitter/active",
        CUTOUT
    );
    private static final ModelTemplate POWERED_DETECTOR_MODEL = ModelTemplates.create(
        "refinedstorage:detector/powered",
        TORCH
    );

    public ModelProviders(final PackOutput output) {
        super(output, MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of();
    }

    @Override
    protected void registerModels(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        registerDiskDrive(blockModels, itemModels);
        registerDiskInterfaces(blockModels, itemModels);
        registerPortableGrids(blockModels, itemModels);
        registerCables(blockModels, itemModels);
        registerControllers(blockModels, itemModels);
        registerImporters(blockModels, itemModels);
        registerExporters(blockModels, itemModels);
        registerExternalStorages(blockModels, itemModels);
        registerConstructors(blockModels, itemModels);
        registerDestructors(blockModels, itemModels);
        registerWirelessTransmitters(blockModels, itemModels);
        registerDetectors(blockModels, itemModels);
        registerNetworkReceivers(blockModels, itemModels);
        registerNetworkTransmitters(blockModels, itemModels);
        registerAutocrafters(blockModels, itemModels);
        registerSecurityManagers(blockModels, itemModels);
        registerRelays(blockModels, itemModels);
        registerMachineCasing(itemModels, blockModels);
        registerInterface(itemModels, blockModels);
        registerStorageBlocks(itemModels, blockModels);
        registerStorageMonitor(itemModels, blockModels);
        registerDirectionalBlock(itemModels, blockModels, "grid", Blocks.INSTANCE.getGrid());
        registerDirectionalBlock(itemModels, blockModels, "crafting_grid", Blocks.INSTANCE.getCraftingGrid());
        registerDirectionalBlock(itemModels, blockModels, "pattern_grid", Blocks.INSTANCE.getPatternGrid());
        registerDirectionalBlock(itemModels, blockModels, "autocrafter_manager",
            Blocks.INSTANCE.getAutocrafterManager());
        registerDirectionalBlock(itemModels, blockModels, "autocrafting_monitor",
            Blocks.INSTANCE.getAutocraftingMonitor());
        registerPatterns(itemModels);
        registerConfigurationCard(itemModels);
        registerNetworkCard(itemModels);
        registerSecurityCard(itemModels);
        registerNetworkBound(itemModels, "wireless_grid", Items.INSTANCE.getWirelessGrid(),
            Items.INSTANCE.getCreativeWirelessGrid());
        registerNetworkBound(itemModels, "wireless_autocrafting_monitor",
            Items.INSTANCE.getWirelessAutocraftingMonitor(),
            Items.INSTANCE.getCreativeWirelessAutocraftingMonitor());
        registerSimpleItems(itemModels);
    }

    private void registerDiskDrive(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getDiskDrive().asItem(), new DiskDriveItemModel.Unbaked());
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getDiskDrive(),
            MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new DiskDriveUnbakedBlockStateModel()))));
    }

    private void registerDiskInterfaces(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        final Identifier back = createIdentifier("block/disk_interface/back");
        final Identifier front = createIdentifier("block/disk_interface/front");
        final Identifier left = createIdentifier("block/disk_interface/left");
        final Identifier right = createIdentifier("block/disk_interface/right");
        final Identifier top = createIdentifier("block/disk_interface/top");

        NORTH_CUTOUT_MODEL.create(
            createIdentifier("block/disk_interface/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(right))
                .put(TextureSlot.NORTH, texture(front))
                .put(TextureSlot.EAST, texture(right))
                .put(TextureSlot.SOUTH, texture(back))
                .put(TextureSlot.WEST, texture(left))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                .put(CUTOUT, texture(createIdentifier("block/disk_interface/cutouts/inactive"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getDiskInterface().forEach((color, id, diskInterface) -> {
            EMISSIVE_NORTH_CUTOUT_MODEL.create(
                createIdentifier("block/disk_interface/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(right))
                    .put(TextureSlot.NORTH, texture(front))
                    .put(TextureSlot.EAST, texture(right))
                    .put(TextureSlot.SOUTH, texture(back))
                    .put(TextureSlot.WEST, texture(left))
                    .put(TextureSlot.UP, texture(top))
                    .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                    .put(CUTOUT, texture(createIdentifier("block/disk_interface/cutouts/" + color.getName()))),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(diskInterface.get().asItem(), new DiskInterfaceItemModel.Unbaked(color));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(diskInterface.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new DiskInterfaceUnbakedBlockStateModel(color)))));
        });
    }

    private void registerPortableGrids(final BlockModelGenerators blockModels,
                                       final ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getPortableGrid(),
            MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new PortableGridUnbakedBlockStateModel()))));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getCreativePortableGrid(),
            MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new PortableGridUnbakedBlockStateModel()))));
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getPortableGrid().asItem(),
            new PortableGridItemModel.Unbaked());
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getCreativePortableGrid().asItem(),
            new PortableGridItemModel.Unbaked());
    }

    private void registerCables(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        Blocks.INSTANCE.getCable().forEach((color, id, cable) -> {
            final Identifier texture = createIdentifier("block/cable/" + color.getName());
            final Identifier coloredItemModel = CABLE_ITEM_MODEL.create(
                createIdentifier("item/cable/" + color.getName()),
                new TextureMapping().put(CABLE, texture(texture)),
                blockModels.modelOutput
            );
            itemModels.itemModelOutput.accept(cable.get().asItem(), ItemModelUtils.plainModel(coloredItemModel));

            CABLE_CORE_MODEL.create(createIdentifier("block/cable/core/" + color.getName()),
                new TextureMapping().put(CABLE, texture(texture)), blockModels.modelOutput);
            CABLE_EXTENSION_MODEL.create(createIdentifier("block/cable/extension/" + color.getName()),
                new TextureMapping().put(CABLE, texture(texture)), blockModels.modelOutput);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(cable.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new CableUnbakedBlockStateModel(color)))));
        });
    }

    private void registerControllers(final BlockModelGenerators blockModels,
                                     final ItemModelGenerators itemModels) {
        final Identifier off = createIdentifier("block/controller/off");
        final Identifier on = createIdentifier("block/controller/on");
        final Identifier nearlyOff = createIdentifier("block/controller/nearly_off");
        final Identifier nearlyOn = createIdentifier("block/controller/nearly_on");
        Blocks.INSTANCE.getController().forEach((color, id, controller) -> {
            final Identifier cutout = createIdentifier("block/controller/cutouts/" + color.getName());
            final TexturedModel model = new TexturedModel(new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(off))
                .put(TextureSlot.ALL, texture(on))
                .put(CUTOUT, texture(cutout)), EMISSIVE_ALL_CUTOUT_MODEL);
            final Identifier modelId = createIdentifier("block/controller/" + color.getName());
            model.getTemplate().create(modelId, model.getMapping(), blockModels.modelOutput);
        });
        Blocks.INSTANCE.getController().forEach((color, id, controller) ->
            registerControllerBlockStates(blockModels, itemModels, color, controller.get(), off, nearlyOff, nearlyOn));
        Blocks.INSTANCE.getCreativeController().forEach((color, id, controller) ->
            registerControllerBlockStates(blockModels, itemModels, color, controller.get(), off, nearlyOff, nearlyOn));
    }

    private static void registerControllerBlockStates(final BlockModelGenerators blockModels,
                                                      final ItemModelGenerators itemModels,
                                                      final DyeColor color,
                                                      final AbstractControllerBlock<?> controller,
                                                      final Identifier off, final Identifier nearlyOff,
                                                      final Identifier nearlyOn) {
        final Identifier model = createIdentifier("block/controller/" + color.getName());
        blockModels.blockStateOutput.accept(MultiPartGenerator.multiPart(controller)
            .with(condition().term(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.OFF),
                plainVariant(off))
            .with(condition().term(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_ON),
                plainVariant(nearlyOn))
            .with(condition().term(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.ON),
                plainVariant(model))
            .with(condition().term(AbstractControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_OFF),
                plainVariant(nearlyOff)));
        itemModels.itemModelOutput.accept(controller.asItem(), ItemModelUtils.rangeSelect(
            new ControllerEnergyLevelItemModelProperty(),
            ItemModelUtils.plainModel(off),
            new RangeSelectItemModel.Entry(0.01F, ItemModelUtils.plainModel(nearlyOff)),
            new RangeSelectItemModel.Entry(0.3F, ItemModelUtils.plainModel(nearlyOn)),
            new RangeSelectItemModel.Entry(0.4F, ItemModelUtils.plainModel(model))
        ));
    }

    private void registerImporters(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        Blocks.INSTANCE.getImporter().forEach((color, id, importer) -> {
            final Identifier itemModel = IMPORTER_ITEM_MODEL.create(
                createIdentifier("item/importer/" + color.getName()),
                new TextureMapping().put(CABLE, texture(createIdentifier("block/cable/" + color.getName()))),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(importer.get().asItem(), ItemModelUtils.plainModel(itemModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(importer.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new CablePartUnbakedBlockStateModel(color, createIdentifier("block/importer/base"))))));
        });
    }

    private void registerExporters(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        Blocks.INSTANCE.getExporter().forEach((color, id, exporter) -> {
            final Identifier itemModel = EXPORTER_ITEM_MODEL.create(
                createIdentifier("item/exporter/" + color.getName()),
                new TextureMapping().put(CABLE, texture(createIdentifier("block/cable/" + color.getName()))),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(exporter.get().asItem(), ItemModelUtils.plainModel(itemModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(exporter.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new CablePartUnbakedBlockStateModel(color, createIdentifier("block/exporter/base"))))));
        });
    }

    private void registerExternalStorages(final BlockModelGenerators blockModels,
                                          final ItemModelGenerators itemModels) {
        Blocks.INSTANCE.getExternalStorage().forEach((color, id, externalStorage) -> {
            final Identifier itemModel = EXTERNAL_STORAGE_ITEM_MODEL.create(
                createIdentifier("item/external_storage/" + color.getName()),
                new TextureMapping().put(CABLE, texture(createIdentifier("block/cable/" + color.getName()))),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(externalStorage.get().asItem(), ItemModelUtils.plainModel(itemModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(externalStorage.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new CablePartUnbakedBlockStateModel(color, createIdentifier("block/external_storage/base"))))));
        });
    }

    private void registerConstructors(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        final Identifier front = createIdentifier("block/constructor/front");
        final Identifier inactiveBlockModel = INACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL.create(
            createIdentifier("block/constructor/inactive"),
            new TextureMapping()
                .put(FRONT, texture(front))
                .put(CUTOUT, texture(createIdentifier("block/constructor/cutouts/inactive"))),
            blockModels.modelOutput
        );
        final Identifier activeBlockModel = ACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL.create(
            createIdentifier("block/constructor/active"),
            new TextureMapping()
                .put(FRONT, texture(front))
                .put(CUTOUT, texture(createIdentifier("block/constructor/cutouts/active"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getConstructor().forEach((color, id, constructor) -> {
            final Identifier itemModel = CONSTRUCTOR_ITEM_MODEL.create(
                createIdentifier("item/constructor/" + color.getName()),
                new TextureMapping().put(CABLE, texture(createIdentifier("block/cable/" + color.getName()))),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(constructor.get().asItem(), ItemModelUtils.plainModel(itemModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(constructor.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new ActiveInactiveCablePartUnbakedBlockStateModel(
                        color,
                        activeBlockModel,
                        inactiveBlockModel
                    )))));
        });
    }

    private void registerDestructors(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        final Identifier front = createIdentifier("block/destructor/front");
        final Identifier inactiveBlockModel = INACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL.create(
            createIdentifier("block/destructor/inactive"),
            new TextureMapping()
                .put(FRONT, texture(front))
                .put(CUTOUT, texture(createIdentifier("block/destructor/cutouts/inactive"))),
            blockModels.modelOutput
        );
        final Identifier activeBlockModel = ACTIVE_CONSTRUCTOR_DESTRUCTOR_MODEL.create(
            createIdentifier("block/destructor/active"),
            new TextureMapping()
                .put(FRONT, texture(front))
                .put(CUTOUT, texture(createIdentifier("block/destructor/cutouts/active"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getDestructor().forEach((color, id, destructor) -> {
            final Identifier itemModel = DESTRUCTOR_ITEM_MODEL.create(
                createIdentifier("item/destructor/" + color.getName()),
                new TextureMapping().put(CABLE, texture(createIdentifier("block/cable/" + color.getName()))),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(destructor.get().asItem(), ItemModelUtils.plainModel(itemModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(destructor.get(),
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(
                    new ActiveInactiveCablePartUnbakedBlockStateModel(
                        color,
                        activeBlockModel,
                        inactiveBlockModel
                    )))));
        });
    }

    private void registerWirelessTransmitters(final BlockModelGenerators blockModels,
                                              final ItemModelGenerators itemModels) {
        final Identifier inactiveBlockModel = createIdentifier("block/wireless_transmitter/inactive");

        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, transmitter) -> {
            final Identifier cutout = createIdentifier("block/wireless_transmitter/cutouts/" + color.getName());

            final Identifier itemModel = ACTIVE_WIRELESS_TRANSMITTER_MODEL.create(
                createIdentifier("item/wireless_transmitter/" + color.getName()),
                new TextureMapping().put(CUTOUT, texture(cutout)),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(transmitter.get().asItem(), ItemModelUtils.plainModel(itemModel));

            final Identifier activeBlockModel = ACTIVE_WIRELESS_TRANSMITTER_MODEL.create(
                createIdentifier("block/wireless_transmitter/" + color.getName()),
                new TextureMapping().put(CUTOUT, texture(cutout)),
                blockModels.modelOutput
            );

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(transmitter.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveBlockModel))
                    .select(true, plainVariant(activeBlockModel)))
                .with(PropertyDispatch.modify(DefaultDirectionType.FACE_CLICKED.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getXRot(direction))
                        .withYRot(getYRot(direction)))));
        });
    }

    private void registerDetectors(final BlockModelGenerators blockModels,
                                   final ItemModelGenerators itemModels) {
        final Identifier unpoweredBlockModel = createIdentifier("block/detector/unpowered");

        Blocks.INSTANCE.getDetector().forEach((color, id, transmitter) -> {
            final Identifier cutout = createIdentifier("block/detector/cutouts/" + color.getName());

            final Identifier itemModel = POWERED_DETECTOR_MODEL.create(
                createIdentifier("item/detector/" + color.getName()),
                new TextureMapping().put(TORCH, texture(cutout)),
                itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(transmitter.get().asItem(), ItemModelUtils.plainModel(itemModel));

            final Identifier poweredBlockModel = POWERED_DETECTOR_MODEL.create(
                createIdentifier("block/detector/" + color.getName()),
                new TextureMapping().put(TORCH, texture(cutout)),
                blockModels.modelOutput
            );

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(transmitter.get())
                .with(PropertyDispatch.initial(DetectorBlock.POWERED)
                    .select(false, plainVariant(unpoweredBlockModel))
                    .select(true, plainVariant(poweredBlockModel)))
                .with(PropertyDispatch.modify(DefaultDirectionType.FACE_CLICKED.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getXRot(direction))
                        .withYRot(getYRot(direction)))));
        });
    }

    private void registerNetworkReceivers(final BlockModelGenerators blockModels,
                                          final ItemModelGenerators itemModels) {
        final Identifier inactiveModel = ALL_CUTOUT_MODEL.create(
            createIdentifier("block/network_receiver/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(createIdentifier("block/network_receiver/base")))
                .put(TextureSlot.ALL, texture(createIdentifier("block/network_receiver/base")))
                .put(CUTOUT, texture(createIdentifier("block/network_receiver/cutouts/inactive"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, receiver) -> {
            final Identifier cutout = createIdentifier("block/network_receiver/cutouts/" + color.getName());
            final Identifier activeModel = EMISSIVE_ALL_CUTOUT_MODEL.create(
                createIdentifier("block/network_receiver/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(createIdentifier("block/network_receiver/base")))
                    .put(TextureSlot.ALL, texture(createIdentifier("block/network_receiver/base")))
                    .put(CUTOUT, texture(cutout)),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(receiver.get().asItem(), ItemModelUtils.plainModel(activeModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(receiver.get())
                .with(PropertyDispatch.initial(NetworkReceiverBlock.ACTIVE)
                    .select(false, plainVariant(inactiveModel))
                    .select(true, plainVariant(activeModel))));
        });
    }

    private void registerNetworkTransmitters(final BlockModelGenerators blockModels,
                                             final ItemModelGenerators itemModels) {
        final Identifier inactiveModel = ALL_CUTOUT_MODEL.create(
            createIdentifier("block/network_transmitter/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(createIdentifier("block/network_transmitter/base")))
                .put(TextureSlot.ALL, texture(createIdentifier("block/network_transmitter/base")))
                .put(CUTOUT, texture(createIdentifier("block/network_transmitter/cutouts/inactive"))),
            blockModels.modelOutput
        );
        final Identifier errorModel = EMISSIVE_ALL_CUTOUT_MODEL.create(
            createIdentifier("block/network_transmitter/error"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(createIdentifier("block/network_transmitter/base")))
                .put(TextureSlot.ALL, texture(createIdentifier("block/network_transmitter/base")))
                .put(CUTOUT, texture(createIdentifier("block/network_transmitter/cutouts/error"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, transmitter) -> {
            final Identifier cutout = createIdentifier("block/network_transmitter/cutouts/" + color.getName());
            final Identifier activeModel = EMISSIVE_ALL_CUTOUT_MODEL.create(
                createIdentifier("block/network_transmitter/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(createIdentifier("block/network_transmitter/base")))
                    .put(TextureSlot.ALL, texture(createIdentifier("block/network_transmitter/base")))
                    .put(CUTOUT, texture(cutout)),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(transmitter.get().asItem(), ItemModelUtils.plainModel(activeModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(transmitter.get())
                .with(PropertyDispatch.initial(NetworkTransmitterBlock.STATE)
                    .select(NetworkTransmitterState.INACTIVE, plainVariant(inactiveModel))
                    .select(NetworkTransmitterState.ERROR, plainVariant(errorModel))
                    .select(NetworkTransmitterState.ACTIVE, plainVariant(activeModel))));
        });
    }

    private void registerAutocrafters(final BlockModelGenerators blockModels,
                                      final ItemModelGenerators itemModels) {
        final Identifier side = createIdentifier("block/autocrafter/side");
        final Identifier top = createIdentifier("block/autocrafter/top");
        final Identifier cutoutSide = createIdentifier("block/autocrafter/cutouts/side/inactive");
        final Identifier cutoutTop = createIdentifier("block/autocrafter/cutouts/top/inactive");

        final Identifier inactiveModel = SIDES_CUTOUT_MODEL.create(
            createIdentifier("block/autocrafter/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(side))
                .put(TextureSlot.NORTH, texture(side))
                .put(TextureSlot.EAST, texture(side))
                .put(TextureSlot.SOUTH, texture(side))
                .put(TextureSlot.WEST, texture(side))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                .put(NORTH_CUTOUT, texture(cutoutSide))
                .put(EAST_CUTOUT, texture(cutoutSide))
                .put(SOUTH_CUTOUT, texture(cutoutSide))
                .put(WEST_CUTOUT, texture(cutoutSide))
                .put(UP_CUTOUT, texture(cutoutTop)),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getAutocrafter().forEach((color, id, autocrafter) -> {
            final Identifier cutoutSideActive = createIdentifier("block/autocrafter/cutouts/side/" + color.getName());
            final Identifier cutoutTopActive = createIdentifier("block/autocrafter/cutouts/top/" + color.getName());
            final Identifier activeModel = EMISSIVE_SIDES_CUTOUT_MODEL.create(
                createIdentifier("block/autocrafter/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(side))
                    .put(TextureSlot.NORTH, texture(side))
                    .put(TextureSlot.EAST, texture(side))
                    .put(TextureSlot.SOUTH, texture(side))
                    .put(TextureSlot.WEST, texture(side))
                    .put(TextureSlot.UP, texture(top))
                    .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                    .put(NORTH_CUTOUT, texture(cutoutSideActive))
                    .put(EAST_CUTOUT, texture(cutoutSideActive))
                    .put(SOUTH_CUTOUT, texture(cutoutSideActive))
                    .put(WEST_CUTOUT, texture(cutoutSideActive))
                    .put(UP_CUTOUT, texture(cutoutTopActive)),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(autocrafter.get().asItem(), ItemModelUtils.plainModel(activeModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(autocrafter.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveModel))
                    .select(true, plainVariant(activeModel)))
                .with(PropertyDispatch.modify(DefaultDirectionType.FACE_CLICKED.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getAutocrafterXRot(direction))
                        .withYRot(getAutocrafterYRot(direction)))));
        });
    }

    private void registerSecurityManagers(final BlockModelGenerators blockModels,
                                          final ItemModelGenerators itemModels) {
        final Identifier back = createIdentifier("block/security_manager/back");
        final Identifier front = createIdentifier("block/security_manager/front");
        final Identifier left = createIdentifier("block/security_manager/left");
        final Identifier right = createIdentifier("block/security_manager/right");
        final Identifier top = createIdentifier("block/security_manager/top");

        final Identifier inactiveModel = SIDES_CUTOUT_MODEL.create(
            createIdentifier("block/security_manager/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(back))
                .put(TextureSlot.NORTH, texture(front))
                .put(TextureSlot.EAST, texture(right))
                .put(TextureSlot.SOUTH, texture(back))
                .put(TextureSlot.WEST, texture(left))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                .put(NORTH_CUTOUT, texture(createIdentifier("block/security_manager/cutouts/front/inactive")))
                .put(EAST_CUTOUT, texture(createIdentifier("block/security_manager/cutouts/right/inactive")))
                .put(SOUTH_CUTOUT, texture(createIdentifier("block/security_manager/cutouts/back/inactive")))
                .put(WEST_CUTOUT, texture(createIdentifier("block/security_manager/cutouts/left/inactive")))
                .put(UP_CUTOUT, texture(createIdentifier("block/security_manager/cutouts/top/inactive"))),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getSecurityManager().forEach((color, id, securityManager) -> {
            final Identifier cutoutBack = createIdentifier(
                "block/security_manager/cutouts/back/" + color.getName()
            );
            final Identifier cutoutFront = createIdentifier(
                "block/security_manager/cutouts/front/" + color.getName()
            );
            final Identifier cutoutLeft = createIdentifier(
                "block/security_manager/cutouts/left/" + color.getName()
            );
            final Identifier cutoutRight = createIdentifier(
                "block/security_manager/cutouts/right/" + color.getName()
            );
            final Identifier cutoutTop = createIdentifier(
                "block/security_manager/cutouts/top/" + color.getName()
            );

            final Identifier activeModel = EMISSIVE_SIDES_CUTOUT_MODEL.create(
                createIdentifier("block/security_manager/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(back))
                    .put(TextureSlot.NORTH, texture(front))
                    .put(TextureSlot.EAST, texture(right))
                    .put(TextureSlot.SOUTH, texture(back))
                    .put(TextureSlot.WEST, texture(left))
                    .put(TextureSlot.UP, texture(top))
                    .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                    .put(NORTH_CUTOUT, texture(cutoutFront))
                    .put(EAST_CUTOUT, texture(cutoutRight))
                    .put(SOUTH_CUTOUT, texture(cutoutBack))
                    .put(WEST_CUTOUT, texture(cutoutLeft))
                    .put(UP_CUTOUT, texture(cutoutTop)),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(securityManager.get().asItem(), ItemModelUtils.plainModel(activeModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(securityManager.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveModel))
                    .select(true, plainVariant(activeModel)))
                .with(PropertyDispatch.modify(HorizontalDirectionType.INSTANCE.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getXRot(OrientedDirection.forHorizontalDirection(direction)))
                        .withYRot(getYRot(OrientedDirection.forHorizontalDirection(direction))))));
        });
    }

    private void registerRelays(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        final Identifier in = createIdentifier("block/relay/in");
        final Identifier out = createIdentifier("block/relay/out");
        final Identifier inactiveOutCutout = createIdentifier("block/relay/cutouts/out/inactive");
        final Identifier inactiveInCutout = createIdentifier("block/relay/cutouts/in/inactive");

        final Identifier inactiveModel = CUTOUT_MODEL.create(
            createIdentifier("block/relay/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(in))
                .put(TextureSlot.NORTH, texture(out))
                .put(TextureSlot.EAST, texture(in))
                .put(TextureSlot.SOUTH, texture(in))
                .put(TextureSlot.WEST, texture(in))
                .put(TextureSlot.UP, texture(in))
                .put(TextureSlot.DOWN, texture(in))
                .put(NORTH_CUTOUT, texture(inactiveOutCutout))
                .put(EAST_CUTOUT, texture(inactiveInCutout))
                .put(SOUTH_CUTOUT, texture(inactiveInCutout))
                .put(WEST_CUTOUT, texture(inactiveInCutout))
                .put(UP_CUTOUT, texture(inactiveInCutout))
                .put(DOWN_CUTOUT, texture(inactiveInCutout)),
            blockModels.modelOutput
        );

        Blocks.INSTANCE.getRelay().forEach((color, id, relay) -> {
            final Identifier cutoutIn = createIdentifier("block/relay/cutouts/in/" + color.getName());
            final Identifier cutoutOut = createIdentifier("block/relay/cutouts/out/" + color.getName());

            final Identifier activeModel = EMISSIVE_CUTOUT_MODEL.create(
                createIdentifier("block/relay/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(in))
                    .put(TextureSlot.NORTH, texture(out))
                    .put(TextureSlot.EAST, texture(in))
                    .put(TextureSlot.SOUTH, texture(in))
                    .put(TextureSlot.WEST, texture(in))
                    .put(TextureSlot.UP, texture(in))
                    .put(TextureSlot.DOWN, texture(in))
                    .put(NORTH_CUTOUT, texture(cutoutOut))
                    .put(EAST_CUTOUT, texture(cutoutIn))
                    .put(SOUTH_CUTOUT, texture(cutoutIn))
                    .put(WEST_CUTOUT, texture(cutoutIn))
                    .put(UP_CUTOUT, texture(cutoutIn))
                    .put(DOWN_CUTOUT, texture(cutoutIn)),
                blockModels.modelOutput
            );

            itemModels.itemModelOutput.accept(relay.get().asItem(), ItemModelUtils.plainModel(activeModel));

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(relay.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveModel))
                    .select(true, plainVariant(activeModel)))
                .with(PropertyDispatch.modify(DefaultDirectionType.FACE_CLICKED.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getXRot(OrientedDirection.forDirection(direction)))
                        .withYRot(getYRot(OrientedDirection.forDirection(direction))))));
        });
    }

    private void registerMachineCasing(final ItemModelGenerators itemModels, final BlockModelGenerators blockModels) {
        final TextureMapping textures = TextureMapping.column(
            texture(createIdentifier("block/machine_casing/side")),
            texture(createIdentifier("block/machine_casing/end"))
        );
        final Identifier machineCasingModel = ModelTemplates.CUBE_COLUMN.create(Blocks.INSTANCE.getMachineCasing(),
            textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getMachineCasing(),
            plainVariant(machineCasingModel)));
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getMachineCasing().asItem(),
            ItemModelUtils.plainModel(machineCasingModel));
    }

    private void registerInterface(final ItemModelGenerators itemModels, final BlockModelGenerators blockModels) {
        final Identifier activeModel = ModelTemplates.CUBE_ALL.create(
            createIdentifier("block/interface/active"),
            TextureMapping.cube(texture(createIdentifier("block/interface/active"))),
            blockModels.modelOutput
        );
        final Identifier inactiveModel = ModelTemplates.CUBE_ALL.create(
            createIdentifier("block/interface/inactive"),
            TextureMapping.cube(texture(createIdentifier("block/interface/inactive"))),
            blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getInterface())
            .with(PropertyDispatch.initial(InterfaceBlock.ACTIVE)
                .select(true, plainVariant(activeModel))
                .select(false, plainVariant(inactiveModel))));
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getInterface().asItem(),
            ItemModelUtils.plainModel(inactiveModel));
    }

    private void registerStorageBlocks(final ItemModelGenerators itemModels, final BlockModelGenerators blockModels) {
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            final Identifier blockModel = ModelTemplates.CUBE_ALL.create(
                createIdentifier("block/storage_block/" + variant.getName() + "_storage_block"),
                TextureMapping.cube(texture(
                    createIdentifier("block/storage_block/" + variant.getName() + "_storage_block"))),
                blockModels.modelOutput
            );
            final Block block = Blocks.INSTANCE.getItemStorageBlock(variant);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(blockModel)));
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(blockModel));
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            final Identifier blockModel = ModelTemplates.CUBE_ALL.create(
                createIdentifier("block/fluid_storage_block/" + variant.getName() + "_fluid_storage_block"),
                TextureMapping.cube(texture(createIdentifier("block/fluid_storage_block/" + variant.getName()
                    + "_fluid_storage_block"))),
                blockModels.modelOutput
            );
            final Block block = Blocks.INSTANCE.getFluidStorageBlock(variant);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(blockModel)));
            itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(blockModel));
        }
    }

    private void registerStorageMonitor(final ItemModelGenerators itemModels, final BlockModelGenerators blockModels) {
        final Identifier blockModel = ModelTemplates.CUBE.create(
            createIdentifier("block/storage_monitor"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(createIdentifier("block/side")))
                .put(TextureSlot.NORTH, texture(createIdentifier("block/storage_monitor/front")))
                .put(TextureSlot.EAST, texture(createIdentifier("block/storage_monitor/left")))
                .put(TextureSlot.SOUTH, texture(createIdentifier("block/storage_monitor/back")))
                .put(TextureSlot.WEST, texture(createIdentifier("block/storage_monitor/right")))
                .put(TextureSlot.UP, texture(createIdentifier("block/storage_monitor/top")))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE)),
            blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INSTANCE.getStorageMonitor(),
                plainVariant(blockModel))
            .with(PropertyDispatch.modify(OrientedDirectionType.INSTANCE.getProperty())
                .generate(direction -> variant -> variant
                    .withXRot(getXRot(direction))
                    .withYRot(getYRot(direction)))));
        itemModels.itemModelOutput.accept(Blocks.INSTANCE.getStorageMonitor().asItem(),
            ItemModelUtils.plainModel(blockModel));
    }

    private void registerDirectionalBlock(final ItemModelGenerators itemModels,
                                          final BlockModelGenerators blockModels,
                                          final String name,
                                          final BlockColorMap<?, ?> blocks) {
        final Identifier right = createIdentifier("block/" + name + "/right");
        final Identifier front = createIdentifier("block/" + name + "/front");
        final Identifier back = createIdentifier("block/" + name + "/back");
        final Identifier left = createIdentifier("block/" + name + "/left");
        final Identifier top = createIdentifier("block/" + name + "/top");
        final Identifier inactiveBlockModel = NORTH_CUTOUT_MODEL.create(
            createIdentifier("block/" + name + "/inactive"),
            new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(right))
                .put(TextureSlot.NORTH, texture(front))
                .put(TextureSlot.EAST, texture(right))
                .put(TextureSlot.SOUTH, texture(back))
                .put(TextureSlot.WEST, texture(left))
                .put(TextureSlot.UP, texture(top))
                .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                .put(CUTOUT, texture(createIdentifier("block/" + name + "/cutouts/inactive"))),
            blockModels.modelOutput
        );
        blocks.forEach((color, id, block) -> {
            final Identifier blockModel = EMISSIVE_NORTH_CUTOUT_MODEL.create(
                createIdentifier("block/" + name + "/" + color.getName()),
                new TextureMapping()
                    .put(TextureSlot.PARTICLE, texture(right))
                    .put(TextureSlot.NORTH, texture(front))
                    .put(TextureSlot.EAST, texture(right))
                    .put(TextureSlot.SOUTH, texture(back))
                    .put(TextureSlot.WEST, texture(left))
                    .put(TextureSlot.UP, texture(top))
                    .put(TextureSlot.DOWN, texture(BOTTOM_TEXTURE))
                    .put(CUTOUT, texture(createIdentifier("block/" + name + "/cutouts/" + color.getName()))),
                blockModels.modelOutput
            );
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block.get())
                .with(PropertyDispatch.initial(AbstractActiveColoredDirectionalBlock.ACTIVE)
                    .select(false, plainVariant(inactiveBlockModel))
                    .select(true, plainVariant(blockModel)))
                .with(PropertyDispatch.modify(OrientedDirectionType.INSTANCE.getProperty())
                    .generate(direction -> variant -> variant
                        .withXRot(getXRot(direction))
                        .withYRot(getYRot(direction)))));
            itemModels.itemModelOutput.accept(block.get().asItem(), ItemModelUtils.plainModel(blockModel));
        });
    }

    private Quadrant getXRot(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH, EAST, SOUTH, WEST -> Quadrant.R0;
            case DOWN_NORTH, DOWN_EAST, DOWN_SOUTH, DOWN_WEST -> Quadrant.R90;
            case UP_NORTH, UP_EAST, UP_SOUTH, UP_WEST -> Quadrant.R270;
        };
    }

    private Quadrant getXRot(final Direction direction) {
        return switch (direction) {
            case DOWN -> Quadrant.R0;
            case UP -> Quadrant.R180;
            case NORTH, SOUTH, WEST, EAST -> Quadrant.R90;
        };
    }

    private Quadrant getYRot(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH, UP_SOUTH, DOWN_NORTH -> Quadrant.R0;
            case EAST, UP_WEST, DOWN_WEST -> Quadrant.R90;
            case SOUTH, UP_NORTH, DOWN_SOUTH -> Quadrant.R180;
            case WEST, UP_EAST, DOWN_EAST -> Quadrant.R270;
        };
    }

    private Quadrant getYRot(final Direction direction) {
        return switch (direction) {
            case DOWN, UP, SOUTH -> Quadrant.R0;
            case NORTH -> Quadrant.R180;
            case EAST -> Quadrant.R270;
            case WEST -> Quadrant.R90;
        };
    }

    private Quadrant getAutocrafterXRot(final Direction direction) {
        return switch (direction) {
            case DOWN -> Quadrant.R0;
            case UP -> Quadrant.R180;
            case NORTH, SOUTH, WEST, EAST -> Quadrant.R90;
        };
    }

    private Quadrant getAutocrafterYRot(final Direction direction) {
        return switch (direction) {
            case DOWN, UP, NORTH -> Quadrant.R0;
            case SOUTH -> Quadrant.R180;
            case EAST -> Quadrant.R90;
            case WEST -> Quadrant.R270;
        };
    }

    private void registerPatterns(final ItemModelGenerators itemModels) {
        final Identifier empty = ModelTemplates.FLAT_ITEM.create(createIdentifier("item/pattern/empty"),
            TextureMapping.layer0(texture(createIdentifier("item/pattern/empty"))), itemModels.modelOutput);
        final Map<PatternType, Identifier> patternModels = Arrays.stream(PatternType.values())
            .collect(Collectors.toMap(type -> type, type -> ModelTemplates.FLAT_ITEM.create(
                createIdentifier("item/pattern/" + type.getSerializedName()),
                TextureMapping.layer0(texture(createIdentifier("item/pattern/" + type.getSerializedName()))),
                itemModels.modelOutput
            ), (a, b) -> a, LinkedHashMap::new));
        itemModels.itemModelOutput.accept(Items.INSTANCE.getPattern(), ItemModelUtils.select(
            new PatternTypeItemModelProperty(),
            ItemModelUtils.plainModel(empty),
            patternModels.entrySet().stream().map(entry -> ItemModelUtils.when(
                entry.getKey(),
                ItemModelUtils.plainModel(entry.getValue())
            )).toList()
        ));
    }

    private void registerConfigurationCard(final ItemModelGenerators itemModels) {
        final Identifier activeItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/configuration_card/active"),
            TextureMapping.layer0(texture(createIdentifier("item/configuration_card/active"))),
            itemModels.modelOutput
        );
        final Identifier inactiveItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/configuration_card/inactive"),
            TextureMapping.layer0(texture(createIdentifier("item/configuration_card/inactive"))),
            itemModels.modelOutput
        );
        itemModels.itemModelOutput.accept(Items.INSTANCE.getConfigurationCard(), ItemModelUtils.conditional(
            new ActiveConfigurationCardItemModelProperty(),
            ItemModelUtils.plainModel(activeItemModel),
            ItemModelUtils.plainModel(inactiveItemModel)
        ));
    }

    private void registerNetworkCard(final ItemModelGenerators itemModels) {
        final Identifier activeItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/network_card/active"),
            TextureMapping.layer0(texture(createIdentifier("item/network_card/active"))),
            itemModels.modelOutput
        );
        final Identifier inactiveItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/network_card/inactive"),
            TextureMapping.layer0(texture(createIdentifier("item/network_card/inactive"))),
            itemModels.modelOutput
        );
        itemModels.itemModelOutput.accept(Items.INSTANCE.getNetworkCard(), ItemModelUtils.conditional(
            new ActiveNetworkCardItemModelProperty(),
            ItemModelUtils.plainModel(activeItemModel),
            ItemModelUtils.plainModel(inactiveItemModel)
        ));
    }

    private void registerSecurityCard(final ItemModelGenerators itemModels) {
        final Identifier activeItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/security_card/active"),
            TextureMapping.layer0(texture(createIdentifier("item/security_card/active"))),
            itemModels.modelOutput
        );
        final Identifier inactiveItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/security_card/inactive"),
            TextureMapping.layer0(texture(createIdentifier("item/security_card/inactive"))),
            itemModels.modelOutput
        );
        itemModels.itemModelOutput.accept(Items.INSTANCE.getSecurityCard(), ItemModelUtils.conditional(
            new ActiveSecurityCardItemModelProperty(),
            ItemModelUtils.plainModel(activeItemModel),
            ItemModelUtils.plainModel(inactiveItemModel)
        ));
    }

    private void registerNetworkBound(final ItemModelGenerators itemModels, final String name, final Item... items) {
        final Identifier activeItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/" + name + "/active"),
            TextureMapping.layer0(texture(createIdentifier("item/" + name + "/active"))),
            itemModels.modelOutput
        );
        final Identifier inactiveItemModel = ModelTemplates.FLAT_ITEM.create(
            createIdentifier("item/" + name + "/inactive"),
            TextureMapping.layer0(texture(createIdentifier("item/" + name + "/inactive"))),
            itemModels.modelOutput
        );
        Arrays.stream(items).forEach(item -> itemModels.itemModelOutput.accept(item, ItemModelUtils.conditional(
            new NetworkBoundItemModelProperty(),
            ItemModelUtils.plainModel(activeItemModel),
            ItemModelUtils.plainModel(inactiveItemModel)
        )));
    }

    private void registerSimpleItems(final ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(Items.INSTANCE.getSilicon(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getWrench(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getRangeUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getCreativeRangeUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getRegulatorUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getAutocraftingUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getSpeedUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getStackUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getSilkTouchUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getConstructionCore(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getDestructionCore(), ModelTemplates.FLAT_ITEM);
        final Identifier fortuneUpgradeId = createIdentifier("item/fortune_upgrade");
        ModelTemplates.FLAT_ITEM.create(fortuneUpgradeId, TextureMapping.layer0(texture(fortuneUpgradeId)),
            itemModels.modelOutput);
        final ItemModel.Unbaked fortuneUpgrade = ItemModelUtils.plainModel(fortuneUpgradeId);
        itemModels.itemModelOutput.accept(Items.INSTANCE.getFortune1Upgrade(), fortuneUpgrade);
        itemModels.itemModelOutput.accept(Items.INSTANCE.getFortune2Upgrade(), fortuneUpgrade);
        itemModels.itemModelOutput.accept(Items.INSTANCE.getFortune3Upgrade(), fortuneUpgrade);
        for (final ProcessorItem.Type type : ProcessorItem.Type.values()) {
            itemModels.generateFlatItem(Items.INSTANCE.getProcessor(type), ModelTemplates.FLAT_ITEM);
        }
        itemModels.generateFlatItem(Items.INSTANCE.getProcessorBinding(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getQuartzEnrichedCopper(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getQuartzEnrichedIron(), ModelTemplates.FLAT_ITEM);
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            itemModels.generateFlatItem(Items.INSTANCE.getItemStorageDisk(variant), ModelTemplates.FLAT_ITEM);
            if (variant != ItemStorageVariant.CREATIVE) {
                itemModels.generateFlatItem(Items.INSTANCE.getItemStoragePart(variant), ModelTemplates.FLAT_ITEM);
            }
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            itemModels.generateFlatItem(Items.INSTANCE.getFluidStorageDisk(variant), ModelTemplates.FLAT_ITEM);
            if (variant != FluidStorageVariant.CREATIVE) {
                itemModels.generateFlatItem(Items.INSTANCE.getFluidStoragePart(variant), ModelTemplates.FLAT_ITEM);
            }
        }
        itemModels.generateFlatItem(Items.INSTANCE.getStorageHousing(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(Items.INSTANCE.getDebugStick(),
            ItemModelUtils.plainModel(Identifier.withDefaultNamespace("item/stick")));
        final Identifier fallbackSecurityCardModel = ModelTemplates.FLAT_ITEM.create(
            Items.INSTANCE.getFallbackSecurityCard(),
            TextureMapping.layer0(texture(createIdentifier("item/security_card/fallback"))),
            itemModels.modelOutput
        );
        itemModels.itemModelOutput.accept(Items.INSTANCE.getFallbackSecurityCard(),
            ItemModelUtils.plainModel(fallbackSecurityCardModel));
    }

    private static Material texture(final Identifier location) {
        return new Material(location);
    }
}
