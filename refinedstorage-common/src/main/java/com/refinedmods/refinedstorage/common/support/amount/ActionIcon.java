package com.refinedmods.refinedstorage.common.support.amount;

import com.refinedmods.refinedstorage.common.support.Sprites;

import net.minecraft.resources.Identifier;

public enum ActionIcon {
    ERROR(Sprites.ERROR),
    START(Sprites.START),
    CANCEL(Sprites.CANCEL),
    RESET(Sprites.RESET),
    SET(Sprites.SET);

    private final Identifier sprite;

    ActionIcon(final Identifier sprite) {
        this.sprite = sprite;
    }

    public Identifier getSprite() {
        return sprite;
    }
}
