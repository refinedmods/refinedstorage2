package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.EnergyUsageListWidget;
import com.refinedmods.refinedstorage.common.support.widget.ProgressWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ControllerScreen extends AbstractBaseScreen<ControllerContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/controller.png");

    private static final int LIST_X = 28;
    private static final int LIST_Y = 20;  // aligned with progress bar top
    private static final int LIST_W = 136;
    private static final int LIST_H = 70;  // matches progress bar height

    private static final int TEXT_COLOR = 0xFF404040;

    @Nullable
    private ProgressWidget progressWidget;
    @Nullable
    private EnergyUsageListWidget listWidget;
    @Nullable
    private ScrollbarWidget listScrollbar;
    private long cachedTotalUsage;

    public ControllerScreen(final ControllerContainerMenu menu,
                            final Inventory playerInventory,
                            final Component title) {
        super(menu, playerInventory, title, 176, 189);
        this.inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(
                getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
                createTranslation("gui", "controller.redstone_mode_help")
        ));

        listScrollbar = new ScrollbarWidget(
                leftPos + LIST_X + 4 + LIST_W - EnergyUsageListWidget.SCROLLBAR_WIDTH,
                topPos + LIST_Y,
                ScrollbarWidget.Type.NORMAL,
                LIST_H
        );

        if (progressWidget == null) {
            progressWidget = new ProgressWidget(
                    leftPos + 8,
                    topPos + 20,
                    16,
                    70,
                    getMenu().getEnergyInfo()::getPercentageFull,
                    this::createProgressTooltip
            );
        } else {
            progressWidget.setX(leftPos + 8);
            progressWidget.setY(topPos + 20);
        }
        addRenderableWidget(progressWidget);

        if (listWidget == null) {
            listWidget = new EnergyUsageListWidget(
                    leftPos + LIST_X,
                    topPos + LIST_Y,
                    LIST_W,
                    LIST_H,
                    listScrollbar
            );
        } else {
            listWidget.setX(leftPos + LIST_X);
            listWidget.setY(topPos + LIST_Y);
        }
        listWidget.setEntries(getMenu().getNodeUsages());
        addRenderableWidget(listWidget);

        cachedTotalUsage = getMenu().getNodeUsages().stream()
                .mapToLong(ControllerBlockEntity.NodeEnergyEntry::usage).sum();
    }

    private List<Component> createProgressTooltip() {
        final List<Component> tooltip = new ArrayList<>(getMenu().getEnergyInfo().createTooltip());
        final long total = getMenu().getNodeUsages().stream()
                .mapToLong(ControllerBlockEntity.NodeEnergyEntry::usage)
                .sum();
        tooltip.add(createTranslation("gui", "controller.total_usage",
                Component.literal(String.valueOf(total)).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GRAY));
        return tooltip;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (listScrollbar != null) {
            listScrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        final Component totalLabel = createTranslation("gui", "controller.total_usage", cachedTotalUsage);
        // No leftPos/topPos — extractLabels uses relative GUI coordinates
        final int labelX = LIST_X + LIST_W - font.width(totalLabel);
        graphics.text(font, totalLabel, labelX, titleLabelY, TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (listScrollbar != null && listScrollbar.mouseClicked(event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (listScrollbar != null) {
            listScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (listScrollbar != null && listScrollbar.mouseReleased(event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (listScrollbar != null
                && isHovering(LIST_X, LIST_Y, LIST_W, LIST_H, x, y)
                && listScrollbar.mouseScrolled(x, y, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}