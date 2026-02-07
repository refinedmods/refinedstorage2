package com.refinedmods.refinedstorage.common.content;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.Nullable;

public final class Sounds {
    public static final Sounds INSTANCE = new Sounds();

    @Nullable
    private Supplier<SoundEvent> wrench;

    private Sounds() {
    }

    public SoundEvent getWrench() {
        return Objects.requireNonNull(wrench).get();
    }

    public void setWrench(final Supplier<SoundEvent> supplier) {
        this.wrench = supplier;
    }
}
