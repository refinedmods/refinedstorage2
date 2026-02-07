package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AutoSelectedSideButtonWidget extends AbstractSideButtonWidget {
    private static final List<MutableComponent> YES_LINES = List.of(
        IdentifierUtil.YES.copy().withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> NO_LINES = List.of(
        IdentifierUtil.NO.copy().withStyle(ChatFormatting.GRAY)
    );

    private static final MutableComponent TITLE = createTranslation("gui", "search_box_auto_selected");
    private static final Identifier YES = createIdentifier("widget/side_button/search_box_auto_selected/yes");
    private static final Identifier NO = createIdentifier("widget/side_button/search_box_auto_selected/no");
    private static final Component HELP = createTranslation("gui", "search_box_auto_selected.help");

    public AutoSelectedSideButtonWidget(final SearchFieldWidget searchFieldWidget) {
        super(createPressAction(searchFieldWidget));
    }

    private static OnPress createPressAction(final SearchFieldWidget searchFieldWidget) {
        return btn -> {
            final boolean autoSelected = !Platform.INSTANCE.getConfig().isSearchBoxAutoSelected();
            Platform.INSTANCE.getConfig().setSearchBoxAutoSelected(autoSelected);
            searchFieldWidget.setAutoSelected(autoSelected);
        };
    }

    @Override
    protected Identifier getSprite() {
        return Platform.INSTANCE.getConfig().isSearchBoxAutoSelected() ? YES : NO;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return Platform.INSTANCE.getConfig().isSearchBoxAutoSelected() ? YES_LINES : NO_LINES;
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
