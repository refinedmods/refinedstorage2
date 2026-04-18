package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewScreen.REQUEST_BUTTON_HEIGHT;
import static com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewScreen.REQUEST_BUTTON_WIDTH;

class AutocraftingRequestButton extends AbstractButton {
    private final AutocraftingRequest request;
    private final TextMarquee text;
    private final Consumer<AutocraftingRequest> onPress;

    AutocraftingRequestButton(final int x,
                              final int y,
                              final AutocraftingRequest request,
                              final Consumer<AutocraftingRequest> onPress) {
        super(x, y, REQUEST_BUTTON_WIDTH, REQUEST_BUTTON_HEIGHT, Component.empty());
        this.request = request;
        final ResourceKey resource = request.getResource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        this.text = new TextMarquee(
            rendering.getDisplayName(resource),
            REQUEST_BUTTON_WIDTH - 16 - 4 - 4 - 4,
            0xFFFFFF,
            true,
            true
        );
        this.onPress = onPress;
    }

    AutocraftingRequest getRequest() {
        return request;
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTicks) {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);
        renderResourceIcon(graphics);
        final int yOffset = SmallText.isSmall() ? 8 : 5;
        text.render(graphics, getX() + 3 + 16 + 3, getY() + yOffset, Minecraft.getInstance().font, isHovered,
            partialTicks);
    }

    private void renderResourceIcon(final GuiGraphics graphics) {
        final ResourceKey resource = request.getResource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        final int resourceX = getX() + 3;
        final int resourceY = getY() + 4;
        rendering.render(resource, graphics, resourceX, resourceY);
        final long normalizedAmount = resource instanceof PlatformResourceKey platformResource
            ? platformResource.getResourceType().normalizeAmount(request.getAmount())
            : 0;
        ResourceSlotRendering.renderAmount(graphics, resourceX, resourceY, normalizedAmount, rendering);
    }

    @Override
    public void onPress() {
        onPress.accept(request);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }
}
