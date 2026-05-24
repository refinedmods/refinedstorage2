package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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

class PinManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PinManager.class);
    private static final Codec<List<GridResource>> CODEC = new ErrorHandlingListCodec<>(
        GridResource.CODEC,
        "Failed to deserialize pin: "
    );
    private static final String FILENAME = "refinedstorage_grid_pins.dat";

    private final List<GridResource> pins = new ArrayList<>();
    private final List<GridResource> pinsView = Collections.unmodifiableList(pins);

    PinManager() {
        final Path path = getSavePath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            final CompoundTag rootTag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            final Tag pinsTag = rootTag.get("pins");
            CODEC.parse(NbtOps.INSTANCE, pinsTag)
                .ifSuccess(pins::addAll)
                .ifError(error -> LOGGER.warn("Failed to load grid pins: {}", error.message()));
        } catch (IOException e) {
            LOGGER.error("Failed to load grid pins", e);
        }
    }

    void add(final int index, final GridResource gridResource) {
        if (contains(gridResource)) {
            return;
        }
        pins.add(index, gridResource);
        save();
    }

    GridResource remove(final int index) {
        final GridResource removed = pins.remove(index);
        save();
        return removed;
    }

    List<GridResource> getAll() {
        return pinsView;
    }

    boolean contains(final GridResource gridResource) {
        for (final GridResource existingPin : pins) {
            if (existingPin.is(gridResource)) {
                return true;
            }
        }
        return false;
    }

    private void save() {
        final Path path = getSavePath();
        try {
            FileUtil.createDirectoriesSafe(path.getParent());
        } catch (IOException e) {
            LOGGER.error("Failed to create directories for grid pins", e);
            return;
        }
        CODEC.encodeStart(NbtOps.INSTANCE, pins).ifSuccess(tag -> {
            try {
                final CompoundTag rootTag = new CompoundTag();
                rootTag.put("pins", tag);
                NbtIo.writeCompressed(rootTag, path);
            } catch (IOException e) {
                LOGGER.error("Failed to save grid pins", e);
            }
        }).ifError(error -> LOGGER.error("Failed to serialize grid pins: {}", error.message()));
    }

    private static Path getSavePath() {
        return Platform.INSTANCE.getGameDirectory().resolve(FILENAME);
    }
}
