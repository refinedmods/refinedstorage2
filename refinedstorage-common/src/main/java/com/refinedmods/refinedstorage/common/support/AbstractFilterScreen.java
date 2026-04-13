package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public abstract class AbstractFilterScreen<T extends AbstractBaseContainerMenu> extends AbstractBaseScreen<T> {
    public static final Identifier TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    protected AbstractFilterScreen(final T menu,
                                   final Inventory playerInventory,
                                   final Component title,
                                   final boolean upgrades) {
        super(menu, playerInventory, title, upgrades ? 210 : 176, 137);
        this.inventoryLabelY = 42;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    public static boolean renderExportingIndicators(final Font font,
                                                    final GuiGraphicsExtractor graphics,
                                                    final int leftPos,
                                                    final int topPos,
                                                    final int mouseX,
                                                    final int mouseY,
                                                    final int indicators,
                                                    final IntFunction<ExportingIndicator> indicatorProvider) {
        for (int i = 0; i < indicators; ++i) {
            final ExportingIndicator indicator = indicatorProvider.apply(i);
            final int xx = leftPos + 7 + (i * 18) + 18 - 10 + 1;
            final int yy = topPos + 19 + 18 - 10 + 1;
            final Identifier sprite = indicator.getSprite();
            if (sprite != null) {
                graphics.blitSprite(GUI_TEXTURED, sprite, xx, yy, WARNING_SIZE, WARNING_SIZE);
            }
            final boolean hovering =
                mouseX >= xx && mouseX <= xx + WARNING_SIZE && mouseY >= yy && mouseY <= yy + WARNING_SIZE;
            if (indicator != ExportingIndicator.NONE && hovering) {
                graphics.tooltip(
                    font,
                    List.of(ClientTooltipComponent.create(indicator.getTooltip().getVisualOrderText())),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
                );
                return true;
            }
        }
        return false;
    }
}
