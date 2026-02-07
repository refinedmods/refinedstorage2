package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridViewType;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class ViewTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.view_type");
    private static final List<MutableComponent> SUBTEXT_ALL = List.of(
        createTranslation("gui", "grid.view_type.all").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_AUTOCRAFTABLE = List.of(
        createTranslation("gui", "grid.view_type.autocraftable").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_NON_AUTOCRAFTABLE = List.of(
        createTranslation("gui", "grid.view_type.non_autocraftable").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier ALL = createIdentifier("widget/side_button/grid/view_type/all");
    private static final Identifier CRAFTABLE =
        createIdentifier("widget/side_button/grid/view_type/autocraftable");
    private static final Identifier NON_CRAFTABLE =
        createIdentifier("widget/side_button/grid/view_type/non_autocraftable");

    private final AbstractGridContainerMenu menu;

    ViewTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setViewType(toggle(menu.getViewType()));
    }

    private static GridViewType toggle(final GridViewType sortingType) {
        return switch (sortingType) {
            case ALL -> GridViewType.AUTOCRAFTABLE;
            case AUTOCRAFTABLE -> GridViewType.NON_AUTOCRAFTABLE;
            case NON_AUTOCRAFTABLE -> GridViewType.ALL;
        };
    }

    @Override
    protected Identifier getSprite() {
        return switch (menu.getViewType()) {
            case ALL -> ALL;
            case AUTOCRAFTABLE -> CRAFTABLE;
            case NON_AUTOCRAFTABLE -> NON_CRAFTABLE;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (menu.getViewType()) {
            case ALL -> SUBTEXT_ALL;
            case AUTOCRAFTABLE -> SUBTEXT_AUTOCRAFTABLE;
            case NON_AUTOCRAFTABLE -> SUBTEXT_NON_AUTOCRAFTABLE;
        };
    }
}
