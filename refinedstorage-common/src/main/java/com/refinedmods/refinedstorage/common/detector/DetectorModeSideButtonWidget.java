package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class DetectorModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "detector.mode");
    private static final List<MutableComponent> SUBTEXT_EQUAL = List.of(
        createTranslation("gui", "detector.mode.equal").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_ABOVE = List.of(
        createTranslation("gui", "detector.mode.above").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_UNDER = List.of(
        createTranslation("gui", "detector.mode.under").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier EQUAL = createIdentifier("widget/side_button/detector_mode/equal");
    private static final Identifier ABOVE = createIdentifier("widget/side_button/detector_mode/above");
    private static final Identifier UNDER = createIdentifier("widget/side_button/detector_mode/under");

    private final ClientProperty<DetectorMode> property;

    DetectorModeSideButtonWidget(final ClientProperty<DetectorMode> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<DetectorMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static DetectorMode toggle(final DetectorMode detectorMode) {
        return switch (detectorMode) {
            case UNDER -> DetectorMode.EQUAL;
            case EQUAL -> DetectorMode.ABOVE;
            case ABOVE -> DetectorMode.UNDER;
        };
    }

    @Override
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case UNDER -> UNDER;
            case EQUAL -> EQUAL;
            case ABOVE -> ABOVE;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (property.getValue()) {
            case UNDER -> SUBTEXT_UNDER;
            case EQUAL -> SUBTEXT_EQUAL;
            case ABOVE -> SUBTEXT_ABOVE;
        };
    }
}
