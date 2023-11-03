package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.constructordestructor.AbstractConstructorDestructorBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerEnergyType;
import com.refinedmods.refinedstorage2.platform.common.detector.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterBlock;
import com.refinedmods.refinedstorage2.platform.common.support.CableBlockSupport;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.support.direction.DirectionTypeImpl;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterBlock;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class BlockStateProvider extends net.minecraftforge.client.model.generators.BlockStateProvider {
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(Map.of(
        Direction.NORTH, CableBlockSupport.NORTH,
        Direction.EAST, CableBlockSupport.EAST,
        Direction.SOUTH, CableBlockSupport.SOUTH,
        Direction.WEST, CableBlockSupport.WEST,
        Direction.UP, CableBlockSupport.UP,
        Direction.DOWN, CableBlockSupport.DOWN
    ));

    private final ExistingFileHelper existingFileHelper;

    public BlockStateProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        registerCables();
        registerExporters();
        registerImporters();
        registerExternalStorages();
        registerControllers();
        registerGrids();
        registerDetectors();
        registerWirelessTransmitters();
        registerConstructorDestructors(Blocks.INSTANCE.getConstructor(), "constructor");
        registerConstructorDestructors(Blocks.INSTANCE.getDestructor(), "destructor");
        registerNetworkReceivers();
        registerNetworkTransmitters();
    }

    private void registerCables() {
        Blocks.INSTANCE.getCable().forEach((color, id, block) -> addCableWithExtensions(block.get(), color));
    }

    private void registerExporters() {
        Blocks.INSTANCE.getExporter().forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = addCableWithExtensions(block.get(), color);
            final ModelFile exporterModel = modelFile(createIdentifier("block/exporter"));
            PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(exporterModel).addModel().condition(DirectionTypeImpl.INSTANCE.getProperty(), direction);
            });
        });
    }

    private void registerImporters() {
        Blocks.INSTANCE.getImporter().forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = addCableWithExtensions(block.get(), color);
            final ModelFile importerModel = modelFile(createIdentifier("block/importer"));
            PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(importerModel).addModel().condition(DirectionTypeImpl.INSTANCE.getProperty(), direction);
            });
        });
    }

    private void registerExternalStorages() {
        Blocks.INSTANCE.getExternalStorage().forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = addCableWithExtensions(block.get(), color);
            final ModelFile model = modelFile(createIdentifier("block/external_storage"));
            PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(model)
                    .addModel()
                    .condition(DirectionTypeImpl.INSTANCE.getProperty(), direction);
            });
        });
    }

    private MultiPartBlockStateBuilder addCableWithExtensions(final Block block, final DyeColor color) {
        final var builder = getMultipartBuilder(block)
            .part()
            .modelFile(modelFile(createIdentifier("block/cable/core/" + color.getName()))).addModel()
            .end();
        final ModelFile extension = modelFile(createIdentifier("block/cable/extension/" + color.getName()));
        addForEachDirection(builder, extension);
        return builder;
    }

    private static void addForEachDirection(final MultiPartBlockStateBuilder builder, final ModelFile extension) {
        PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
            final var part = builder.part();
            addDirectionalRotation(direction, part);
            part.modelFile(extension).addModel().condition(property, true);
        });
    }

    private static void addDirectionalRotation(
        final Direction direction,
        final ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> part
    ) {
        switch (direction) {
            case UP -> part.rotationX(270);
            case SOUTH -> part.rotationX(180);
            case DOWN -> part.rotationX(90);
            case WEST -> part.rotationY(270);
            case EAST -> part.rotationY(90);
        }
    }

    private void registerGrids() {
        Blocks.INSTANCE.getGrid().forEach((color, id, block) -> configureGridVariants(color, block, "grid"));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) -> configureGridVariants(
            color,
            block,
            "crafting_grid"
        ));
    }

    private void configureGridVariants(final DyeColor color,
                                       final Supplier<? extends AbstractGridBlock<?>> block,
                                       final String name) {
        final ModelFile inactive = modelFile(createIdentifier("block/" + name + "/inactive"));
        final ModelFile active = modelFile(createIdentifier("block/" + name + "/" + color.getName()));
        final var builder = getVariantBuilder(block.get());
        builder.forAllStates(blockState -> {
            final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
            if (Boolean.TRUE.equals(blockState.getValue(AbstractGridBlock.ACTIVE))) {
                model.modelFile(active);
            } else {
                model.modelFile(inactive);
            }
            addRotation(model, blockState.getValue(BiDirectionType.INSTANCE.getProperty()));
            return model.build();
        });
    }

    private void registerControllers() {
        Blocks.INSTANCE.getController().forEach((color, id, block) -> configureControllerVariants(color, block));
        Blocks.INSTANCE.getCreativeController().forEach(
            (color, id, block) -> configureControllerVariants(color, block)
        );
    }

    private void configureControllerVariants(final DyeColor color, final Supplier<? extends Block> block) {
        final ModelFile off = modelFile(createIdentifier("block/controller/off"));
        final ModelFile nearlyOff = modelFile(createIdentifier("block/controller/nearly_off"));
        final ModelFile nearlyOn = modelFile(createIdentifier("block/controller/nearly_on"));
        final var builder = getVariantBuilder(block.get());
        builder.addModels(
            builder.partialState().with(ControllerBlock.ENERGY_TYPE, ControllerEnergyType.OFF),
            ConfiguredModel.builder().modelFile(off).buildLast()
        );
        builder.addModels(
            builder.partialState().with(ControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_OFF),
            ConfiguredModel.builder().modelFile(nearlyOff).buildLast()
        );
        builder.addModels(
            builder.partialState().with(ControllerBlock.ENERGY_TYPE, ControllerEnergyType.NEARLY_ON),
            ConfiguredModel.builder().modelFile(nearlyOn).buildLast()
        );
        builder.addModels(
            builder.partialState().with(ControllerBlock.ENERGY_TYPE, ControllerEnergyType.ON),
            ConfiguredModel.builder()
                .modelFile(modelFile(createIdentifier("block/controller/" + color.getName())))
                .buildLast()
        );
    }

    private void registerDetectors() {
        final ModelFile unpowered = modelFile(createIdentifier("block/detector/unpowered"));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> registerDetector(unpowered, block.get(), blockState));
        });
    }

    private void registerWirelessTransmitters() {
        final ModelFile inactive = modelFile(createIdentifier("block/wireless_transmitter/inactive"));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(WirelessTransmitterBlock.ACTIVE))) {
                    model.modelFile(modelFile(createIdentifier("block/wireless_transmitter/" + color.getName())));
                } else {
                    model.modelFile(inactive);
                }
                final Direction direction = blockState.getValue(DirectionTypeImpl.INSTANCE.getProperty());
                addRotation(model, direction);
                return model.build();
            });
        });
    }

    private void registerConstructorDestructors(final BlockColorMap<?> blockMap, final String type) {
        blockMap.forEach((color, id, block) -> {
            final MultiPartBlockStateBuilder builder = addCableWithExtensions(block.get(), color);
            final ModelFile activeModel = modelFile(createIdentifier("block/" + type + "/active"));
            final ModelFile inactiveModel = modelFile(createIdentifier("block/" + type + "/inactive"));
            PROPERTY_BY_DIRECTION.forEach((direction, property) -> {
                final var part = builder.part();
                addDirectionalRotation(direction, part);
                part.modelFile(activeModel)
                    .addModel()
                    .condition(DirectionTypeImpl.INSTANCE.getProperty(), direction)
                    .condition(AbstractConstructorDestructorBlock.ACTIVE, true)
                    .end();
                part.modelFile(inactiveModel)
                    .addModel()
                    .condition(DirectionTypeImpl.INSTANCE.getProperty(), direction)
                    .condition(AbstractConstructorDestructorBlock.ACTIVE, false)
                    .end();
            });
        });
    }

    private ConfiguredModel[] registerDetector(final ModelFile unpowered,
                                               final DetectorBlock block,
                                               final BlockState blockState) {
        final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
        if (Boolean.TRUE.equals(blockState.getValue(DetectorBlock.POWERED))) {
            model.modelFile(modelFile(createIdentifier("block/detector/" + block.getColor().getName())));
        } else {
            model.modelFile(unpowered);
        }
        final Direction direction = blockState.getValue(DirectionTypeImpl.INSTANCE.getProperty());
        addRotation(model, direction);
        return model.build();
    }

    private void registerNetworkReceivers() {
        final ModelFile inactive = modelFile(createIdentifier("block/network_receiver/inactive"));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                if (Boolean.TRUE.equals(blockState.getValue(NetworkReceiverBlock.ACTIVE))) {
                    model.modelFile(modelFile(createIdentifier("block/network_receiver/" + color.getName())));
                } else {
                    model.modelFile(inactive);
                }
                return model.build();
            });
        });
    }

    private void registerNetworkTransmitters() {
        final ModelFile inactive = modelFile(createIdentifier("block/network_transmitter/inactive"));
        final ModelFile error = modelFile(createIdentifier("block/network_transmitter/error"));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) -> {
            final var builder = getVariantBuilder(block.get());
            builder.forAllStates(blockState -> {
                final ConfiguredModel.Builder<?> model = ConfiguredModel.builder();
                switch (blockState.getValue(NetworkTransmitterBlock.STATE)) {
                    case ACTIVE ->
                        model.modelFile(modelFile(createIdentifier("block/network_transmitter/" + color.getName())));
                    case ERROR -> model.modelFile(error);
                    case INACTIVE -> model.modelFile(inactive);
                }
                return model.build();
            });
        });
    }

    private void addRotation(final ConfiguredModel.Builder<?> model, final Direction direction) {
        final int rotationX;
        if (direction.getAxis() == Direction.Axis.Y) {
            rotationX = direction == Direction.UP ? 180 : 0;
        } else {
            rotationX = direction.getAxis().isHorizontal() ? 90 : 0;
        }
        final int rotationY = direction.getAxis().isVertical() ? 0 : ((int) direction.toYRot()) % 360;
        model.rotationX(rotationX);
        model.rotationY(rotationY);
    }

    private void addRotation(final ConfiguredModel.Builder<?> model, final BiDirection direction) {
        final int x = (int) direction.getVec().x();
        final int y = (int) direction.getVec().y();
        final int z = (int) direction.getVec().z();
        switch (direction) {
            case UP_EAST, UP_NORTH, UP_SOUTH, UP_WEST, DOWN_EAST, DOWN_WEST, DOWN_SOUTH, DOWN_NORTH ->
                model.rotationX(x * -1).rotationY(z);
            case EAST, WEST -> model.rotationY(y + 180);
            case NORTH, SOUTH -> model.rotationY(y);
        }
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, existingFileHelper);
    }
}
