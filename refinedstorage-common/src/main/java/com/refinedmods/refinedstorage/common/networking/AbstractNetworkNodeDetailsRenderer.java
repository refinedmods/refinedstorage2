package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.networking.NetworkNodeDetailsRenderer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.SLOT;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public abstract class AbstractNetworkNodeDetailsRenderer implements NetworkNodeDetailsRenderer {
    protected static final int PADDING = 4;
    protected static final int COLUMNS = 9;
    protected static final int SLOT_SIZE = 18;
    protected static final int PROGRESS_BAR_HEIGHT = 16;
    protected static final int TEXT_COLOR = 0xFF404040;
    private static final int TOP_PADDING = 5;

    private static final Component ACTIVE = createTranslation("gui", "network_monitor.details.active");
    private static final Component INACTIVE = createTranslation("gui", "network_monitor.details.inactive");

    @Override
    public final List<ClientTooltipComponent> render(final NetworkNodeDetails details,
                                                     final GuiGraphicsExtractor graphics,
                                                     final int x,
                                                     final int scrollY,
                                                     final int baseY,
                                                     final int width,
                                                     final int height,
                                                     final int mouseX,
                                                     final int mouseY) {
        if (!(details instanceof AbstractNetworkNodeDetails baseDetails)) {
            return Collections.emptyList();
        }
        renderBase(graphics, x, scrollY, width, baseDetails);
        return renderAdditionalDetails(details, graphics, x, scrollY + getHeaderHeight(), baseY, width, height,
            mouseX, mouseY);
    }

    private static void renderBase(final GuiGraphicsExtractor graphics, final int x, final int y, final int width,
                                   final AbstractNetworkNodeDetails baseDetails) {
        final Font font = Minecraft.getInstance().font;
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        final Component energyUsage = createTranslation("gui", "network_monitor.details.energy_usage_per_tick",
            format(baseDetails.getEnergyUsage()));
        SmallText.render(graphics, font, energyUsage.getVisualOrderText(), x + PADDING, y + TOP_PADDING, TEXT_COLOR,
            false, scale);
        final Component activeInactive = baseDetails.isActive() ? ACTIVE : INACTIVE;
        SmallText.render(graphics, font, activeInactive.getVisualOrderText(),
            x + width - PADDING - (int) (font.width(activeInactive) * scale), y + TOP_PADDING, TEXT_COLOR, false,
            scale);
    }

    @Override
    public final int getHeight(final NetworkNodeDetails details) {
        return getHeaderHeight() + getAdditionalHeight(details);
    }

    protected static int getLineHeight() {
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        return (int) (Minecraft.getInstance().font.lineHeight * scale) + PADDING;
    }

    protected static void renderLine(final GuiGraphicsExtractor graphics,
                                     final int x,
                                     final int y,
                                     final Component text) {
        SmallText.render(graphics, Minecraft.getInstance().font, text.getVisualOrderText(), x + PADDING, y,
            TEXT_COLOR, false, SmallText.correctScale(SmallText.DEFAULT_SCALE));
    }

    protected static void renderRightAlignedLine(final GuiGraphicsExtractor graphics,
                                                 final int x,
                                                 final int y,
                                                 final int width,
                                                 final Component text) {
        final Font font = Minecraft.getInstance().font;
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        SmallText.render(graphics, font, text.getVisualOrderText(),
            x + width - PADDING - (int) (font.width(text) * scale), y, TEXT_COLOR, false, scale);
    }

    protected static int getSlotRows(final int slots) {
        return (int) Math.ceil((float) slots / (float) COLUMNS);
    }

    protected static boolean isHoveringSlot(final int slotX, final int slotY, final int mouseX, final int mouseY) {
        return mouseX >= slotX && mouseY >= slotY && mouseX <= slotX + 16 && mouseY <= slotY + 16;
    }

    protected static List<ClientTooltipComponent> renderResourceSlots(final GuiGraphicsExtractor graphics,
                                                                      final int x,
                                                                      final int y,
                                                                      final int mouseX,
                                                                      final int mouseY,
                                                                      final List<Optional<ResourceKey>> resources,
                                                                      final int slots) {
        ResourceKey hovering = null;
        for (int i = 0; i < slots; ++i) {
            final int slotX = x + (i % COLUMNS) * SLOT_SIZE;
            final int slotY = y + (i / COLUMNS) * SLOT_SIZE;
            graphics.blitSprite(GUI_TEXTURED, SLOT, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            final ResourceKey resource = i < resources.size() ? resources.get(i).orElse(null) : null;
            if (resource == null) {
                continue;
            }
            final boolean interact = isHoveringSlot(slotX, slotY, mouseX, mouseY);
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightBack(graphics, slotX + 1, slotY + 1);
                hovering = resource;
            }
            RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass())
                .render(resource, graphics, slotX + 1, slotY + 1);
            if (interact) {
                ClientPlatformUtil.renderSlotHighlightFront(graphics, slotX + 1, slotY + 1);
            }
        }
        return getResourceTooltip(hovering, graphics, mouseX);
    }

    private static List<ClientTooltipComponent> getResourceTooltip(@Nullable final ResourceKey resource,
                                                                   final GuiGraphicsExtractor graphics,
                                                                   final int mouseX) {
        if (resource == null) {
            return Collections.emptyList();
        }
        final ItemStack stackContext = resource instanceof ItemResource itemResource
            ? itemResource.toItemStack()
            : ItemStack.EMPTY;
        return Platform.INSTANCE.processTooltipComponents(
            stackContext,
            graphics,
            mouseX,
            Optional.empty(),
            RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass()).getTooltip(resource)
        );
    }

    private static int getHeaderHeight() {
        return TOP_PADDING + getLineHeight();
    }

    protected abstract List<ClientTooltipComponent> renderAdditionalDetails(NetworkNodeDetails details,
                                                                            GuiGraphicsExtractor graphics,
                                                                            int x,
                                                                            int scrollY,
                                                                            int baseY,
                                                                            int width,
                                                                            int height,
                                                                            int mouseX,
                                                                            int mouseY);

    protected abstract int getAdditionalHeight(NetworkNodeDetails details);
}
