package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.InWorldExternalPatternSinkKey;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class AutocraftingMonitorItemTooltip implements ClientTooltipComponent {
    private static final int SPACING = 2;

    private static final MutableComponent MACHINE_DOES_NOT_ACCEPT_RESOURCE = createTranslation(
        "gui",
        "autocrafting_monitor.machine_does_not_accept_resource"
    ).withStyle(ChatFormatting.RED);
    private static final MutableComponent NO_MACHINE_FOUND = createTranslation(
        "gui",
        "autocrafting_monitor.no_machine_found"
    ).withStyle(ChatFormatting.RED);
    private static final MutableComponent AUTOCRAFTER_IS_LOCKED = createTranslation(
        "gui",
        "autocrafting_monitor.autocrafter_is_locked"
    ).withStyle(ChatFormatting.RED);

    private final TaskStatus.Item item;
    private final ResourceRendering rendering;

    AutocraftingMonitorItemTooltip(final TaskStatus.Item item) {
        this.item = item;
        this.rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(item.resource().getClass());
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        int yy = y;
        graphics.text(
            font,
            rendering.getDisplayName(item.resource()),
            x,
            yy,
            0xFFFFFFFF
        );
        yy += 9 + SPACING;
        if (item.type() != TaskStatus.ItemType.NORMAL) {
            graphics.text(
                font,
                getErrorTooltip(item.type()),
                x,
                yy,
                0xFFAAAAAA
            );
            yy += 9 + SPACING;
        }
        if (item.sinkKey() instanceof InWorldExternalPatternSinkKey(String name, ItemStack stack)) {
            graphics.item(stack, x, yy);
            graphics.text(
                font,
                name,
                x + 18 + SPACING,
                yy + 4,
                0xFFAAAAAA
            );
        }
    }

    @Override
    public int getHeight(final Font font) {
        return font.lineHeight + SPACING
            + (item.type() != TaskStatus.ItemType.NORMAL ? font.lineHeight + SPACING : 0)
            + (item.sinkKey() != null ? 18 : 0);
    }

    @Override
    public int getWidth(final Font font) {
        final int resourceWidth = font.width(rendering.getDisplayName(item.resource()));
        final int errorWidth = item.type() != TaskStatus.ItemType.NORMAL ? font.width(getErrorTooltip(item.type())) : 0;
        final int sinkWidth = item.sinkKey() instanceof InWorldExternalPatternSinkKey sinkKey
            ? (18 + SPACING + font.width(sinkKey.name()))
            : 0;
        return Math.max(resourceWidth, Math.max(errorWidth, sinkWidth));
    }

    private Component getErrorTooltip(final TaskStatus.ItemType type) {
        return switch (type) {
            case REJECTED -> MACHINE_DOES_NOT_ACCEPT_RESOURCE;
            case NONE_FOUND -> NO_MACHINE_FOUND;
            case LOCKED -> AUTOCRAFTER_IS_LOCKED;
            default -> Component.empty();
        };
    }
}
