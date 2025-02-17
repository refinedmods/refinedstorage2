package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractFilterScreen<T extends AbstractBaseContainerMenu> extends AbstractBaseScreen<T> {
    public static final ResourceLocation TEXTURE = createIdentifier("textures/gui/generic_filter.png");

    protected AbstractFilterScreen(final T menu,
                                   final Inventory playerInventory,
                                   final Component title) {
        super(menu, playerInventory, title);
        this.inventoryLabelY = 42;
        this.imageWidth = hasUpgrades() ? 210 : 176;
        this.imageHeight = 137;
    }

    protected boolean hasUpgrades() {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    protected final boolean renderExportingIndicators(final GuiGraphics graphics,
                                                      final int x,
                                                      final int y,
                                                      final int indicators,
                                                      final IntFunction<ExportingIndicator> indicatorProvider) {
        for (int i = 0; i < indicators; ++i) {
            final ExportingIndicator indicator = indicatorProvider.apply(i);
            final int xx = leftPos + 7 + (i * 18) + 18 - 10 + 1;
            final int yy = topPos + 19 + 18 - 10 + 1;
            final ResourceLocation sprite = indicator.getSprite();
            if (sprite != null) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 300);
                graphics.blitSprite(sprite, xx, yy, WARNING_SIZE, WARNING_SIZE);
                graphics.pose().popPose();
            }
            if (indicator != ExportingIndicator.NONE
                && isHovering(xx - leftPos, yy - topPos, WARNING_SIZE, WARNING_SIZE, x, y)) {
                Platform.INSTANCE.renderTooltip(
                    graphics,
                    List.of(ClientTooltipComponent.create(indicator.getTooltip().getVisualOrderText())),
                    x,
                    y
                );
                return true;
            }
        }
        return false;
    }
}
