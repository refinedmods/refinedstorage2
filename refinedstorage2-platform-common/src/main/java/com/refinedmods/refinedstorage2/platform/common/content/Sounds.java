package com.refinedmods.refinedstorage2.platform.common.content;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;

public final class Sounds {
    public static final Sounds INSTANCE = new Sounds();

    @Nullable
    private Supplier<SoundEvent> wrench;

    private Sounds() {
    }

    public SoundEvent getWrench() {
        return Objects.requireNonNull(wrench).get();
    }

    public void setWrench(final Supplier<SoundEvent> wrenchSupplier) {
        this.wrench = wrenchSupplier;
    }
}
