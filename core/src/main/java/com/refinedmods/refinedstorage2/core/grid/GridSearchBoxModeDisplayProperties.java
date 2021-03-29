package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GridSearchBoxModeDisplayProperties {
    private final Identifier spriteIdentifier;
    private final int x;
    private final int y;
    private final Text name;

    public GridSearchBoxModeDisplayProperties(Identifier spriteIdentifier, int x, int y, Text name) {
        this.spriteIdentifier = spriteIdentifier;
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Identifier getSpriteIdentifier() {
        return spriteIdentifier;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Text getName() {
        return name;
    }
}
