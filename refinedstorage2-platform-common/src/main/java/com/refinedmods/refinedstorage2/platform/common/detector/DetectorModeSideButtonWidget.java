package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DetectorModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "detector.mode");
    private static final MutableComponent SUBTEXT_EQUAL = createTranslation("gui", "detector.mode.equal");
    private static final MutableComponent SUBTEXT_ABOVE = createTranslation("gui", "detector.mode.above");
    private static final MutableComponent SUBTEXT_UNDER = createTranslation("gui", "detector.mode.under");

    private final ClientProperty<DetectorMode> property;

    public DetectorModeSideButtonWidget(final ClientProperty<DetectorMode> property) {
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
    protected int getXTexture() {
        return switch (property.getValue()) {
            case UNDER -> 0;
            case EQUAL -> 16;
            case ABOVE -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 176;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (property.getValue()) {
            case UNDER -> SUBTEXT_UNDER;
            case EQUAL -> SUBTEXT_EQUAL;
            case ABOVE -> SUBTEXT_ABOVE;
        };
    }
}
