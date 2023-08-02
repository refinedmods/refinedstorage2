package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class RedstoneModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "redstone_mode");
    private static final MutableComponent SUBTEXT_IGNORE = createTranslation("gui", "redstone_mode.ignore");
    private static final MutableComponent SUBTEXT_HIGH = createTranslation("gui", "redstone_mode.high");
    private static final MutableComponent SUBTEXT_LOW = createTranslation("gui", "redstone_mode.low");
    private static final Component HELP_IGNORE = createTranslation("gui", "redstone_mode.ignore.help");
    private static final Component HELP_HIGH = createTranslation("gui", "redstone_mode.high.help");
    private static final Component HELP_LOW = createTranslation("gui", "redstone_mode.low.help");

    private final ClientProperty<RedstoneMode> property;
    private final Component helpIgnore;
    private final Component helpHigh;
    private final Component helpLow;

    public RedstoneModeSideButtonWidget(final ClientProperty<RedstoneMode> property) {
        this(property, null);
    }

    public RedstoneModeSideButtonWidget(final ClientProperty<RedstoneMode> property,
                                        @Nullable final Component extraHelpText) {
        super(createPressAction(property));
        this.property = property;
        this.helpIgnore = getHelpText(HELP_IGNORE, extraHelpText);
        this.helpHigh = getHelpText(HELP_HIGH, extraHelpText);
        this.helpLow = getHelpText(HELP_LOW, extraHelpText);
    }

    private static OnPress createPressAction(final ClientProperty<RedstoneMode> property) {
        return btn -> property.setValue(property.getValue().toggle());
    }

    @Override
    protected int getXTexture() {
        return switch (property.getValue()) {
            case IGNORE -> 0;
            case HIGH -> 16;
            case LOW -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 0;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (property.getValue()) {
            case IGNORE -> SUBTEXT_IGNORE;
            case HIGH -> SUBTEXT_HIGH;
            case LOW -> SUBTEXT_LOW;
        };
    }

    @Override
    protected Component getHelpText() {
        return switch (property.getValue()) {
            case IGNORE -> helpIgnore;
            case HIGH -> helpHigh;
            case LOW -> helpLow;
        };
    }

    private Component getHelpText(final Component text, @Nullable final Component extraText) {
        if (extraText == null) {
            return text;
        }
        return text.copy().append(" ").append(extraText);
    }
}
