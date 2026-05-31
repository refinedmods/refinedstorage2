package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePinRepository implements PinRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePinRepository.class);
    private static final Codec<List<GridResource>> CODEC = new ErrorHandlingListCodec<>(
        GridResource.CODEC,
        "Failed to deserialize pin: "
    );

    private final Path path;

    private FilePinRepository(final Path path) {
        this.path = path;
    }

    public static PinRepository create() {
        return new FilePinRepository(Platform.INSTANCE.getGameDirectory().resolve("refinedstorage_grid_pins.dat"));
    }

    @Override
    public void saveAll(final List<Pin> pins) {
        try {
            FileUtil.createDirectoriesSafe(path.getParent());
        } catch (IOException e) {
            LOGGER.error("Failed to create directories for grid pins", e);
            return;
        }
        final List<GridResource> gridResources = pins.stream()
            .filter(Pin::manual)
            .map(Pin::gridResource)
            .toList();
        CODEC.encodeStart(NbtOps.INSTANCE, gridResources).ifSuccess(tag -> {
            try {
                final CompoundTag rootTag = new CompoundTag();
                rootTag.put("pins", tag);
                NbtIo.writeCompressed(rootTag, path);
            } catch (IOException e) {
                LOGGER.error("Failed to save grid pins", e);
            }
        }).ifError(error -> LOGGER.error("Failed to serialize grid pins: {}", error.message()));
    }

    @Override
    public List<Pin> loadAll() {
        if (!Files.exists(path)) {
            return List.of();
        }
        final List<Pin> pins = new ArrayList<>();
        try {
            final CompoundTag rootTag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            final Tag pinsTag = rootTag.get("pins");
            CODEC.parse(NbtOps.INSTANCE, pinsTag)
                .ifSuccess(gridResources ->
                    gridResources.forEach(gridResource -> pins.add(new Pin(gridResource, true))))
                .ifError(error -> LOGGER.warn("Failed to load grid pins: {}", error.message()));
        } catch (IOException e) {
            LOGGER.error("Failed to load grid pins", e);
        }
        return pins;
    }
}
