package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.sounds.SoundEvent;

public final class Sounds {
    public static final Sounds INSTANCE = new Sounds();

    private SoundEvent wrench;

    private Sounds() {
    }

    public SoundEvent getWrench() {
        return wrench;
    }

    public void setWrench(SoundEvent wrench) {
        this.wrench = wrench;
    }
}
