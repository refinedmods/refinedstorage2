package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FilterModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "filter_mode");
    private static final MutableComponent SUBTEXT_BLOCK = createTranslation("gui", "filter_mode.block");
    private static final MutableComponent SUBTEXT_ALLOW = createTranslation("gui", "filter_mode.allow");
    private static final Component FILTER_MODE_WARNING = createTranslation("gui", "storage.filter_mode.empty_warning");

    private final ClientProperty<FilterMode> property;
    private final Component helpAllow;
    private final Component helpBlock;

    public FilterModeSideButtonWidget(final ClientProperty<FilterMode> property,
                                      final Component helpAllow,
                                      final Component helpBlock) {
        super(createPressAction(property));
        this.property = property;
        this.helpAllow = helpAllow;
        this.helpBlock = helpBlock;
    }

    public void setWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(FILTER_MODE_WARNING);
        } else {
            setWarning(null);
        }
    }

    private static OnPress createPressAction(final ClientProperty<FilterMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static FilterMode toggle(final FilterMode filterMode) {
        return filterMode == FilterMode.ALLOW ? FilterMode.BLOCK : FilterMode.ALLOW;
    }

    @Override
    protected int getXTexture() {
        return property.getValue() == FilterMode.BLOCK ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return property.getValue() == FilterMode.BLOCK ? SUBTEXT_BLOCK : SUBTEXT_ALLOW;
    }

    @Override
    protected Component getHelpText() {
        return property.getValue() == FilterMode.BLOCK ? helpBlock : helpAllow;
    }
}
