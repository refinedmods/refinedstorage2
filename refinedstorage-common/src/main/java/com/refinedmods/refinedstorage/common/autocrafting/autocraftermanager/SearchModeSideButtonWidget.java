package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class SearchModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "autocrafter_manager.search_mode");
    private static final List<MutableComponent> SUBTEXT_ALL = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.all").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_PATTERN_INPUTS = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_inputs").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_PATTERN_OUTPUTS = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_outputs").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_AUTOCRAFTER_NAMES = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.autocrafter_names").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier SPRITE_ALL =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/all");
    private static final Identifier SPRITE_PATTERN_INPUTS =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/pattern_inputs");
    private static final Identifier SPRITE_PATTERN_OUTPUTS =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/pattern_outputs");
    private static final Identifier SPRITE_AUTOCRAFTER_NAMES =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/autocrafter_names");

    private final AutocrafterManagerContainerMenu containerMenu;
    private final Supplier<Component> helpTextSupplier;

    SearchModeSideButtonWidget(final AutocrafterManagerContainerMenu containerMenu,
                               final Supplier<Component> helpTextSupplier) {
        super(createPressAction(containerMenu));
        this.containerMenu = containerMenu;
        this.helpTextSupplier = helpTextSupplier;
    }

    private static OnPress createPressAction(final AutocrafterManagerContainerMenu containerMenu) {
        return btn -> containerMenu.setSearchMode(containerMenu.getSearchMode().toggle());
    }

    @Override
    protected Identifier getSprite() {
        return switch (containerMenu.getSearchMode()) {
            case ALL -> SPRITE_ALL;
            case PATTERN_INPUTS -> SPRITE_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> SPRITE_PATTERN_OUTPUTS;
            case AUTOCRAFTER_NAMES -> SPRITE_AUTOCRAFTER_NAMES;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (containerMenu.getSearchMode()) {
            case ALL -> SUBTEXT_ALL;
            case PATTERN_INPUTS -> SUBTEXT_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> SUBTEXT_PATTERN_OUTPUTS;
            case AUTOCRAFTER_NAMES -> SUBTEXT_AUTOCRAFTER_NAMES;
        };
    }

    @Override
    protected Component getHelpText() {
        return helpTextSupplier.get();
    }
}
