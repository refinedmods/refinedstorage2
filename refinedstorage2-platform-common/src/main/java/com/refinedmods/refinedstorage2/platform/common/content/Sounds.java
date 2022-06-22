package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;

public final class Sounds {
    public static final Sounds INSTANCE = new Sounds();

    private Supplier<SoundEvent> wrench;

    private Sounds() {
    }

    public SoundEvent getWrench() {
        return wrench.get();
    }

    public void setWrench(Supplier<SoundEvent> wrenchSupplier) {
        this.wrench = wrenchSupplier;
    }
}
