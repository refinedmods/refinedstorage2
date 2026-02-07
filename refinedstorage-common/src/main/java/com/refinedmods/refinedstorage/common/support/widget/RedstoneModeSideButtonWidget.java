package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class RedstoneModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "redstone_mode");
    private static final List<MutableComponent> SUBTEXT_IGNORE = List.of(
        createTranslation("gui", "redstone_mode.ignore").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_HIGH = List.of(
        createTranslation("gui", "redstone_mode.high").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_LOW = List.of(
        createTranslation("gui", "redstone_mode.low").withStyle(ChatFormatting.GRAY)
    );
    private static final Component HELP_IGNORE = createTranslation("gui", "redstone_mode.ignore.help");
    private static final Component HELP_HIGH = createTranslation("gui", "redstone_mode.high.help");
    private static final Component HELP_LOW = createTranslation("gui", "redstone_mode.low.help");
    private static final Identifier IGNORE = createIdentifier("widget/side_button/redstone_mode/ignore");
    private static final Identifier HIGH = createIdentifier("widget/side_button/redstone_mode/high");
    private static final Identifier LOW = createIdentifier("widget/side_button/redstone_mode/low");

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
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case IGNORE -> IGNORE;
            case HIGH -> HIGH;
            case LOW -> LOW;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
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
