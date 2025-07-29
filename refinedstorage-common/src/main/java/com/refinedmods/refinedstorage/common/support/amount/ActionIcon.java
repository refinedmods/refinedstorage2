package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.support.Sprites;

import net.minecraft.resources.ResourceLocation;

public enum ActionIcon {
    ERROR(Sprites.ERROR),
    START(Sprites.START),
    CANCEL(Sprites.CANCEL),
    RESET(Sprites.RESET),
    SET(Sprites.SET);

    private final ResourceLocation sprite;

    ActionIcon(final ResourceLocation sprite) {
        this.sprite = sprite;
    }

    public ResourceLocation getSprite() {
        return sprite;
    }
}
