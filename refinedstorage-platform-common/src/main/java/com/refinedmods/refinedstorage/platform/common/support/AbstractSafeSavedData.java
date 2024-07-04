package com.refinedmods.refinedstorage.platform.common.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public abstract class AbstractSafeSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void save(final File file, final HolderLookup.Provider provider) {
        if (!isDirty()) {
            return;
        }
        final var targetPath = file.toPath().toAbsolutePath();
        final var tempFile = targetPath.getParent().resolve(file.getName() + ".temp");
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", this.save(new CompoundTag(), provider));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            doSave(compoundTag, tempFile, targetPath);
        } catch (final IOException e) {
            LOGGER.error("Could not save data {}", this, e);
        }
        setDirty(false);
    }

    private void doSave(final CompoundTag compoundTag,
                        final Path tempFile,
                        final Path targetPath) throws IOException {
        // Write to temp file first.
        NbtIo.writeCompressed(compoundTag, tempFile);
        // Try atomic move
        try {
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException ignored) {
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
