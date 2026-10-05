package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.ProgressBarWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createStoredWithCapacityTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class PlatformNetworkNodeDetailsRenderer extends AbstractNetworkNodeDetailsRenderer {
    @Override
    protected List<ClientTooltipComponent> renderAdditionalDetails(final NetworkNodeDetails details,
                                                                   final GuiGraphicsExtractor graphics,
                                                                   final int x,
                                                                   final int scrollY,
                                                                   final int baseY,
                                                                   final int width,
                                                                   final int height,
                                                                   final int mouseX,
                                                                   final int mouseY) {
        if (!(details instanceof PlatformNetworkNodeDetails platformDetails)) {
            return Collections.emptyList();
        }
        final boolean mouseInBounds = mouseY >= baseY && mouseY < baseY + height;
        List<ClientTooltipComponent> tooltip = Collections.emptyList();
        int y = scrollY;
        for (final NetworkNodeDetailsElement element : platformDetails.getElements()) {
            final int elementHeight = getHeight(element, width);
            if (y + elementHeight > baseY && y < baseY + height) {
                final List<ClientTooltipComponent> elementTooltip = render(element, graphics, x, y, width,
                    mouseInBounds ? mouseX : Integer.MIN_VALUE, mouseInBounds ? mouseY : Integer.MIN_VALUE);
                if (!elementTooltip.isEmpty()) {
                    tooltip = elementTooltip;
                }
            }
            y += elementHeight;
        }
        return tooltip;
    }

    @Override
    protected int getAdditionalHeight(final NetworkNodeDetails details) {
        if (!(details instanceof PlatformNetworkNodeDetails platformDetails)) {
            return 0;
        }
        // The width is fixed, as the renderer doesn't know about the width when calculating the height.
        return platformDetails.getElements()
            .stream()
            .mapToInt(element -> getHeight(element, NetworkMonitorScreen.DETAILS_WIDTH))
            .sum();
    }

    private static int getHeight(final NetworkNodeDetailsElement element, final int width) {
        return switch (element) {
            case NetworkNodeDetailsElement.Text text -> getLines(text, width).size() * getLineHeight();
            case NetworkNodeDetailsElement.Resources resources ->
                getSlotRows(resources.resources().size()) * SLOT_SIZE + PADDING;
            case NetworkNodeDetailsElement.Items items -> getSlotRows(items.items().size()) * SLOT_SIZE + PADDING;
            case NetworkNodeDetailsElement.ResourceAmounts resourceAmounts ->
                getSlotRows(resourceAmounts.resources().size()) * SLOT_SIZE + PADDING;
            case NetworkNodeDetailsElement.Progress ignored -> getLineHeight() + PROGRESS_BAR_HEIGHT + PADDING;
        };
    }

    private static List<FormattedCharSequence> getLines(final NetworkNodeDetailsElement.Text text,
                                                        final int width) {
        if (text.rightText().isPresent()) {
            return List.of(text.text().getVisualOrderText());
        }
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        return Minecraft.getInstance().font.split(text.text(), (int) ((width - (PADDING * 2)) / scale));
    }

    private static List<ClientTooltipComponent> render(final NetworkNodeDetailsElement element,
                                                       final GuiGraphicsExtractor graphics,
                                                       final int x,
                                                       final int y,
                                                       final int width,
                                                       final int mouseX,
                                                       final int mouseY) {
        return switch (element) {
            case NetworkNodeDetailsElement.Text text -> {
                renderText(text, graphics, x, y, width);
                yield Collections.emptyList();
            }
            case NetworkNodeDetailsElement.Resources resources -> renderResourceSlots(graphics, x, y, mouseX,
                mouseY, resources.resources(), resources.resources().size());
            case NetworkNodeDetailsElement.Items items -> renderItemSlots(graphics, x, y, mouseX, mouseY,
                items.items());
            case NetworkNodeDetailsElement.ResourceAmounts resourceAmounts -> renderResourceAmountSlots(graphics,
                x, y, mouseX, mouseY, resourceAmounts.resources());
            case NetworkNodeDetailsElement.Progress progress -> {
                renderProgress(progress, graphics, x, y, width);
                yield Collections.emptyList();
            }
        };
    }

    private static void renderText(final NetworkNodeDetailsElement.Text text,
                                   final GuiGraphicsExtractor graphics,
                                   final int x,
                                   final int y,
                                   final int width) {
        final Font font = Minecraft.getInstance().font;
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        int lineY = y;
        for (final FormattedCharSequence line : getLines(text, width)) {
            SmallText.render(graphics, font, line, x + PADDING, lineY, TEXT_COLOR, false, scale);
            lineY += getLineHeight();
        }
        text.rightText().ifPresent(rightText -> renderRightAlignedLine(graphics, x, y, width, rightText));
    }

    private static void renderProgress(final NetworkNodeDetailsElement.Progress progress,
                                       final GuiGraphicsExtractor graphics,
                                       final int x,
                                       final int y,
                                       final int width) {
        renderLine(graphics, x, y, createStoredWithCapacityTranslation(
            progress.stored(),
            progress.capacity(),
            progress.getProgress(),
            TEXT_COLOR
        ).withColor(TEXT_COLOR));
        ProgressBarWidget.renderHorizontal(graphics, x + PADDING, y + getLineHeight(), width - (PADDING * 2),
            PROGRESS_BAR_HEIGHT, progress.getProgress());
    }

    private static List<ClientTooltipComponent> renderResourceAmountSlots(
        final GuiGraphicsExtractor graphics,
        final int x,
        final int y,
        final int mouseX,
        final int mouseY,
        final List<Optional<ResourceAmount>> resources
    ) {
        final List<ClientTooltipComponent> tooltip = renderResourceSlots(graphics, x, y, mouseX, mouseY,
            resources.stream().map(resource -> resource.map(ResourceAmount::resource)).toList(), resources.size());
        for (int i = 0; i < resources.size(); ++i) {
            final ResourceAmount resource = resources.get(i).orElse(null);
            if (resource == null) {
                continue;
            }
            final int slotX = x + (i % COLUMNS) * SLOT_SIZE;
            final int slotY = y + (i / COLUMNS) * SLOT_SIZE;
            ResourceSlotRendering.renderAmount(graphics, slotX + 1, slotY + 1, resource.amount(),
                RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.resource().getClass()));
        }
        return tooltip;
    }

    private static List<ClientTooltipComponent> renderItemSlots(final GuiGraphicsExtractor graphics,
                                                                final int x,
                                                                final int y,
                                                                final int mouseX,
                                                                final int mouseY,
                                                                final List<ItemStack> items) {
        final Font font = Minecraft.getInstance().font;
        ItemStack hovering = null;
        for (int i = 0; i < items.size(); ++i) {
            final int slotX = x + (i % COLUMNS) * SLOT_SIZE;
            final int slotY = y + (i / COLUMNS) * SLOT_SIZE;
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            final ItemStack stack = items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            final boolean interact = isHoveringSlot(slotX, slotY, mouseX, mouseY);
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightBack(graphics, slotX + 1, slotY + 1);
                hovering = stack;
            }
            graphics.item(stack, slotX + 1, slotY + 1);
            graphics.itemDecorations(font, stack, slotX + 1, slotY + 1);
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightFront(graphics, slotX + 1, slotY + 1);
            }
        }
        return getItemTooltip(hovering, graphics, mouseX);
    }

    private static List<ClientTooltipComponent> getItemTooltip(@Nullable final ItemStack stack,
                                                               final GuiGraphicsExtractor graphics,
                                                               final int mouseX) {
        if (stack == null) {
            return Collections.emptyList();
        }
        return Platform.INSTANCE.processTooltipComponents(
            stack,
            graphics,
            mouseX,
            stack.getTooltipImage(),
            Screen.getTooltipFromItem(Minecraft.getInstance(), stack)
        );
    }

    static boolean containsItem(final NetworkNodeDetails details, final ItemStack stack) {
        if (!(details instanceof PlatformNetworkNodeDetails platformDetails)) {
            return false;
        }
        for (final NetworkNodeDetailsElement element : platformDetails.getElements()) {
            if (element instanceof NetworkNodeDetailsElement.Items(List<ItemStack> items)) {
                for (final ItemStack item : items) {
                    if (item == stack) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
