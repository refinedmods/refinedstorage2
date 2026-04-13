package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.support.widget.CustomButton;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class PatternTypeButton extends CustomButton {
    private static final ItemStack CRAFTING_ICON = Items.CRAFTING_TABLE.getDefaultInstance();
    private static final ItemStack PROCESSING_ICON = Items.FURNACE.getDefaultInstance();
    private static final ItemStack STONECUTTER_ICON = Items.STONECUTTER.getDefaultInstance();
    private static final ItemStack SMITHING_TABLE_ICON = Items.SMITHING_TABLE.getDefaultInstance();

    private static final WidgetSprites GENERIC_SMALL_BUTTON_SPRITES = new WidgetSprites(
        createIdentifier("widget/generic_small_button"),
        createIdentifier("widget/generic_small_button_disabled"),
        createIdentifier("widget/generic_small_button_focused"),
        createIdentifier("widget/generic_small_button_disabled")
    );

    private final ItemStack icon;
    private boolean selected;

    PatternTypeButton(final int x,
                      final int y,
                      final Consumer<CustomButton> onPress,
                      final PatternType patternType,
                      final boolean selected) {
        super(x, y, 16, 16, GENERIC_SMALL_BUTTON_SPRITES, onPress, patternType.getTranslatedName());
        this.selected = selected;
        this.icon = getIcon(patternType);
        this.setTooltip(Tooltip.create(patternType.getTranslatedName()));
    }

    void setSelected(final boolean selected) {
        this.selected = selected;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int x, final int y,
                                final float partialTicks) {
        final Identifier location = sprites.get(isActive(), isHovered() || selected);
        graphics.blitSprite(GUI_TEXTURED, location, getX(), getY(), width, height);
        graphics.item(icon, getX(), getY());
    }

    private static ItemStack getIcon(final PatternType type) {
        return switch (type) {
            case CRAFTING -> CRAFTING_ICON;
            case PROCESSING -> PROCESSING_ICON;
            case STONECUTTER -> STONECUTTER_ICON;
            case SMITHING_TABLE -> SMITHING_TABLE_ICON;
        };
    }
}
