package com.refinedmods.refinedstorage.common.support;

import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class Sprites {
    public static final Identifier LIGHT_ARROW = createIdentifier("light_arrow");
    public static final Identifier SLOT = createIdentifier("slot");
    public static final int LIGHT_ARROW_WIDTH = 22;
    public static final int LIGHT_ARROW_HEIGHT = 15;
    public static final Identifier WARNING = createIdentifier("warning");
    public static final Identifier AUTOCRAFTING_INDICATOR = createIdentifier("autocrafting_indicator");
    public static final int WARNING_SIZE = 10;
    public static final int ICON_SIZE = 12;
    public static final Identifier ERROR = createIdentifier("error");
    public static final Identifier START = createIdentifier("start");
    public static final Identifier CANCEL = createIdentifier("cancel");
    public static final Identifier RESET = createIdentifier("reset");
    public static final Identifier SET = createIdentifier("set");

    private Sprites() {
    }
}
