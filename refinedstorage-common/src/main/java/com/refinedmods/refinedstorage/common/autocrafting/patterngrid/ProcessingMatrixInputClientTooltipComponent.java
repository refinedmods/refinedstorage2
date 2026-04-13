package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceTag;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import java.util.List;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class ProcessingMatrixInputClientTooltipComponent implements ClientTooltipComponent {
    private static final long CYCLE_MS = 1000;
    private static final MutableComponent ALLOWED_ALTERNATIVES =
        createTranslation("gui", "pattern_grid.processing.allowed_alternatives")
            .withStyle(ChatFormatting.YELLOW);
    private static final int PADDING = 2;

    private final List<ResourceTag> allowedAlternatives;
    private final int width;
    private final int height;
    private final List<Component> names;

    private long cycleStart = 0;
    private int currentCycle = 0;

    ProcessingMatrixInputClientTooltipComponent(final PlatformResourceKey resource,
                                                final Set<Identifier> allowedAlternativeIds) {
        this.allowedAlternatives = resource.getTags()
            .stream()
            .filter(tag -> allowedAlternativeIds.contains(tag.key().location()))
            .toList();
        this.names = allowedAlternatives.stream().map(ProcessingMatrixInputClientTooltipComponent::getName).toList();
        int totalWidth = 0;
        for (final Component name : names) {
            totalWidth = Math.max(totalWidth, 18 + PADDING + Minecraft.getInstance().font.width(name));
        }
        this.width = totalWidth;
        this.height = 9 + PADDING + 18 * allowedAlternatives.size() + 3;
    }

    @Override
    public void extractImage(final Font font, final int x, final int y, final int w, final int h,
                             final GuiGraphicsExtractor graphics) {
        final long now = System.currentTimeMillis();
        if (cycleStart == 0) {
            cycleStart = now;
        }
        if (now - cycleStart >= CYCLE_MS) {
            currentCycle++;
            cycleStart = now;
        }

        graphics.text(font, ALLOWED_ALTERNATIVES, x, y, 0xFFFFFFFF);

        for (int i = 0; i < allowedAlternatives.size(); i++) {
            final ResourceTag alternative = allowedAlternatives.get(i);
            final PlatformResourceKey resource = alternative.resources().get(
                currentCycle % alternative.resources().size()
            );
            final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
                resource.getClass()
            );
            rendering.render(resource, graphics, x, y + 9 + PADDING + i * 18);
            graphics.text(
                font,
                names.get(i),
                x + 18 + PADDING,
                y + 9 + PADDING + i * 18 + (18 / 2) - (9 / 2),
                0xFFAAAAAA
            );
        }
    }

    @Override
    public int getHeight(final Font font) {
        return height;
    }

    @Override
    public int getWidth(final Font font) {
        return width;
    }

    private static Component getName(final ResourceTag alternative) {
        final String translationKey = IdentifierUtil.getTagTranslationKey(alternative.key());
        final boolean hasTranslation = I18n.exists(translationKey);
        return hasTranslation
            ? Component.translatable(translationKey)
            : Component.literal(alternative.key().location().toString());
    }
}
