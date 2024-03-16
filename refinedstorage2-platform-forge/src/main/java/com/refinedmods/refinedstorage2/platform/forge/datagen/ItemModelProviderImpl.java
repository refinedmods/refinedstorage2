package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.constructordestructor.ConstructorBlock;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.DestructorBlock;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ColorMap;
import com.refinedmods.refinedstorage2.platform.common.exporter.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.importer.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.externalstorage.ExternalStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterBlock;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ItemModelProviderImpl extends ItemModelProvider {
    private static final String CUTOUT_TEXTURE_KEY = "cutout";
    private static final String CABLE_TEXTURE_KEY = "cable";

    public ItemModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerExporters();
        registerImporters();
        registerExternalStorages();
        registerControllers();
        registerCreativeControllers();
        registerGrids();
        registerCraftingGrids();
        registerDetectors();
        registerConstructors();
        registerDestructors();
        registerWirelessTransmitters();
        registerNetworkReceivers();
        registerNetworkTransmitters();
    }

    private void registerCables() {
        final ResourceLocation base = createIdentifier("item/cable/base");
        final ColorMap<CableBlock> blocks = Blocks.INSTANCE.getCable();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerExporters() {
        final ResourceLocation base = createIdentifier("item/exporter/base");
        final ColorMap<ExporterBlock> blocks = Blocks.INSTANCE.getExporter();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerImporters() {
        final ResourceLocation base = createIdentifier("item/importer/base");
        final ColorMap<ImporterBlock> blocks = Blocks.INSTANCE.getImporter();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerExternalStorages() {
        final ResourceLocation base = createIdentifier("item/external_storage/base");
        final ColorMap<ExternalStorageBlock> blocks = Blocks.INSTANCE.getExternalStorage();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerControllers() {
        final ResourceLocation base = new ResourceLocation("item/generated");
        final ResourceLocation off = createIdentifier("block/controller/off");
        final ResourceLocation nearlyOff = createIdentifier("block/controller/nearly_off");
        final ResourceLocation nearlyOn = createIdentifier("block/controller/nearly_on");
        final ResourceLocation stored = createIdentifier("stored_in_controller");
        final var blocks = Blocks.INSTANCE.getController();
        blocks.forEach((color, id, block) ->
            withExistingParent(id.getPath(), base)
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

    private void registerCreativeControllers() {
        final var blocks = Blocks.INSTANCE.getCreativeController();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/controller/" + color.getName())
        ));
    }

    private void registerGrids() {
        final var blocks = Blocks.INSTANCE.getGrid();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/grid/" + color.getName())
        ));
    }

    private void registerCraftingGrids() {
        final var blocks = Blocks.INSTANCE.getCraftingGrid();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/crafting_grid/" + color.getName())
        ));
    }

    private void registerDetectors() {
        final var blocks = Blocks.INSTANCE.getDetector();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/detector/" + color.getName())
        ));
    }

    private void registerConstructors() {
        final ResourceLocation base = createIdentifier("item/constructor/base");
        final ColorMap<ConstructorBlock> blocks = Blocks.INSTANCE.getConstructor();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerDestructors() {
        final ResourceLocation base = createIdentifier("item/destructor/base");
        final ColorMap<DestructorBlock> blocks = Blocks.INSTANCE.getDestructor();
        blocks.forEach((color, id, block) -> addCableTexture(color, id, base));
    }

    private void registerWirelessTransmitters() {
        final ResourceLocation base = createIdentifier("block/wireless_transmitter/inactive");
        final ColorMap<WirelessTransmitterBlock> blocks = Blocks.INSTANCE.getWirelessTransmitter();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            CUTOUT_TEXTURE_KEY,
            createIdentifier("block/wireless_transmitter/cutouts/" + color.getName())
        ));
    }

    private void registerNetworkReceivers() {
        final ResourceLocation base = createIdentifier("block/network_receiver/inactive");
        final ColorMap<NetworkReceiverBlock> blocks = Blocks.INSTANCE.getNetworkReceiver();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            CUTOUT_TEXTURE_KEY,
            createIdentifier("block/network_receiver/cutouts/" + color.getName())
        ));
    }

    private void registerNetworkTransmitters() {
        final ResourceLocation base = createIdentifier("block/network_transmitter/inactive");
        final ColorMap<NetworkTransmitterBlock> blocks = Blocks.INSTANCE.getNetworkTransmitter();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            CUTOUT_TEXTURE_KEY,
            createIdentifier("block/network_transmitter/cutouts/" + color.getName())
        ));
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, existingFileHelper);
    }

    private void addCableTexture(final DyeColor color,
                                 final ResourceLocation id,
                                 final ResourceLocation base) {
        singleTexture(
            id.getPath(),
            base,
            CABLE_TEXTURE_KEY,
            createIdentifier("block/cable/" + color.getName())
        );
    }
}
