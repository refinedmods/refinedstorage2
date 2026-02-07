package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.common.support.amount.PriorityScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class AutocrafterPrioritySideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "priority");
    private static final Component HELP = createTranslation("gui", "priority.autocrafter_help");
    private static final Identifier SPRITE = createIdentifier("widget/side_button/priority");

    private final ClientProperty<Integer> property;

    AutocrafterPrioritySideButtonWidget(final ClientProperty<Integer> property,
                                        final Inventory playerInventory,
                                        final Screen parent) {
        super(createPressAction(property, playerInventory, parent));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<Integer> property,
                                             final Inventory playerInventory,
                                             final Screen parent) {
        return btn -> Minecraft.getInstance().setScreen(
            new PriorityScreen(TITLE, property.get(), property::setValue, parent, playerInventory)
        );
    }

    @Override
    protected Identifier getSprite() {
        return SPRITE;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of(Component.literal(String.valueOf(property.getValue())).withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
