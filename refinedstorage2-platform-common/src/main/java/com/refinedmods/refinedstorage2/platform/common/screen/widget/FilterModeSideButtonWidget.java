package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import java.util.List;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FilterModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "filter_mode");
    private static final MutableComponent SUBTEXT_BLOCK = createTranslation("gui", "filter_mode.block");
    private static final MutableComponent SUBTEXT_ALLOW = createTranslation("gui", "filter_mode.allow");
    private static final List<MutableComponent> HELP_BLOCK =
        List.of(createTranslation("gui", "filter_mode.block.help"));
    private static final List<MutableComponent> HELP_ALLOW =
        List.of(createTranslation("gui", "filter_mode.allow.help"));

    private final ClientProperty<FilterMode> property;

    public FilterModeSideButtonWidget(final ClientProperty<FilterMode> property) {
        super(createPressAction(property));
        this.property = property;
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
    protected List<MutableComponent> getHelpText() {
        return property.getValue() == FilterMode.BLOCK ? HELP_BLOCK : HELP_ALLOW;
    }
}
