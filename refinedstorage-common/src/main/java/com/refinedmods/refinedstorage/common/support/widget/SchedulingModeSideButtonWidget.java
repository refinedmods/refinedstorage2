package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class SchedulingModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "scheduling_mode");
    private static final Identifier DEFAULT = createIdentifier("widget/side_button/scheduling_mode/default");
    private static final Identifier ROUND_ROBIN =
        createIdentifier("widget/side_button/scheduling_mode/round_robin");
    private static final Identifier RANDOM = createIdentifier("widget/side_button/scheduling_mode/random");

    private final ClientProperty<SchedulingModeType> property;

    public SchedulingModeSideButtonWidget(final ClientProperty<SchedulingModeType> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<SchedulingModeType> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static SchedulingModeType toggle(final SchedulingModeType modeSettings) {
        return switch (modeSettings) {
            case DEFAULT -> SchedulingModeType.ROUND_ROBIN;
            case ROUND_ROBIN -> SchedulingModeType.RANDOM;
            case RANDOM -> SchedulingModeType.DEFAULT;
        };
    }

    @Override
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case DEFAULT -> DEFAULT;
            case ROUND_ROBIN -> ROUND_ROBIN;
            case RANDOM -> RANDOM;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of(property.getValue().getName().copy().withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected Component getHelpText() {
        return property.getValue().getHelpText();
    }
}
