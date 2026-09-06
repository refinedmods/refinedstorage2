package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.ProgressBarWidget;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createStoredWithCapacityTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class StorageContentsNetworkNodeDetailsRenderer extends AbstractNetworkNodeDetailsRenderer {
    private static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int PROGRESS_BAR_HEIGHT = 16;

    @Override
    protected List<ClientTooltipComponent> renderDetails(final NetworkNodeDetails details,
                                                         final GuiGraphicsExtractor graphics,
                                                         final int x,
                                                         final int y,
                                                         final int visibleY,
                                                         final int visibleHeight,
                                                         final int mouseX,
                                                         final int mouseY) {
        if (!(details instanceof StorageContentsNetworkNodeDetails contentsDetails)) {
            return Collections.emptyList();
        }
        final int storedHeight = getStoredHeight();
        if (y + storedHeight > visibleY && y < visibleY + visibleHeight) {
            renderStored(graphics, x, y, contentsDetails);
        }
        return renderContents(graphics, x, y + storedHeight, visibleY, visibleHeight, mouseX, mouseY, contentsDetails);
    }

    @Override
    protected int getDetailsHeight(final NetworkNodeDetails details) {
        if (details instanceof StorageContentsNetworkNodeDetails contentsDetails) {
            final int size = contentsDetails.getContents().size();
            final int rows = (int) Math.ceil((float) size / (float) COLUMNS);
            return getStoredHeight() + (rows * SLOT_SIZE);
        }
        return 0;
    }

    private static int getStoredHeight() {
        return smallTextLineHeight() + PROGRESS_BAR_HEIGHT + PADDING;
    }

    private static void renderStored(final GuiGraphicsExtractor graphics,
                                     final int x,
                                     final int y,
                                     final StorageContentsNetworkNodeDetails details) {
        final Font font = Minecraft.getInstance().font;
        final Component stored = details.hasCapacity()
            ? createStoredWithCapacityTranslation(details.getStored(), details.getCapacity(), details.getProgress(),
            TEXT_COLOR).withColor(TEXT_COLOR)
            : createTranslation("misc", "stored", format(details.getStored())).withColor(TEXT_COLOR);
        SmallText.render(graphics, font, stored.getVisualOrderText(), x + PADDING, y, TEXT_COLOR, false,
            smallTextScale());
        ProgressBarWidget.renderHorizontal(graphics, x + PADDING, y + smallTextLineHeight(),
            WIDTH - (PADDING * 2), PROGRESS_BAR_HEIGHT, details.getProgress());
    }

    private static List<ClientTooltipComponent> renderContents(final GuiGraphicsExtractor graphics,
                                                               final int x,
                                                               final int y,
                                                               final int visibleY,
                                                               final int visibleHeight,
                                                               final int mouseX,
                                                               final int mouseY,
                                                               final StorageContentsNetworkNodeDetails details) {
        final List<ResourceAmount> contents = details.getContents();
        ResourceAmount hovering = null;
        for (int i = getFirstVisibleIndex(y, visibleY); i < contents.size(); ++i) {
            final int slotY = y + (i / COLUMNS) * SLOT_SIZE;
            if (slotY >= visibleY + visibleHeight) {
                break;
            }
            final ResourceAmount content = contents.get(i);
            final int slotX = x + (i % COLUMNS) * SLOT_SIZE;
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            final boolean interact = mouseX >= slotX
                && mouseY >= slotY
                && mouseX <= slotX + 16
                && mouseY <= slotY + 16;
            renderSlotContents(graphics, slotX + 1, slotY + 1, content, interact);
            if (interact) {
                hovering = content;
            }
        }
        return hovering != null ? getTooltip(hovering, graphics, mouseX) : Collections.emptyList();
    }

    private static int getFirstVisibleIndex(final int y, final int visibleY) {
        final int hiddenRows = Math.max(0, Math.floorDiv(visibleY - y, SLOT_SIZE));
        return hiddenRows * COLUMNS;
    }

    private static void renderSlotContents(final GuiGraphicsExtractor graphics,
                                           final int x,
                                           final int y,
                                           final ResourceAmount content,
                                           final boolean interact) {
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightBack(graphics, x, y);
        }
        final ResourceRendering rendering = getRendering(content.resource());
        rendering.render(content.resource(), graphics, x, y);
        ResourceSlotRendering.renderAmount(graphics, x, y, content.amount(), rendering);
        if (interact) {
            ClientPlatformUtil.renderSlotHighlightFront(graphics, x, y);
        }
    }

    private static List<ClientTooltipComponent> getTooltip(final ResourceAmount content,
                                                           final GuiGraphicsExtractor graphics,
                                                           final int mouseX) {
        final ResourceKey resource = content.resource();
        final ResourceRendering rendering = getRendering(resource);
        final ItemStack stackContext = resource instanceof ItemResource itemResource
            ? itemResource.toItemStack()
            : ItemStack.EMPTY;
        final List<ClientTooltipComponent> lines = Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            Optional.empty(),
            rendering.getTooltip(resource)
        );
        lines.add(new SmallTextClientTooltipComponent(
            createTranslation("misc", "total", rendering.formatAmount(content.amount()))
                .withStyle(ChatFormatting.GRAY)
        ));
        return lines;
    }

    private static ResourceRendering getRendering(final ResourceKey resource) {
        return RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
    }
}
