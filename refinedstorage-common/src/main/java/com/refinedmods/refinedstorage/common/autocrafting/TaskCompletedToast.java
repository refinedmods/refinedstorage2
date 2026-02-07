package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class TaskCompletedToast implements Toast {
    private static final Identifier SPRITE = createIdentifier("autocrafting_task_completed_toast");
    private static final MutableComponent TITLE = createTranslation(
        "misc",
        "autocrafting_task_completed"
    );

    private static final long TIME_VISIBLE = 5000;
    private static final int MARGIN = 4;

    private final ResourceKey resource;
    private final ResourceRendering rendering;
    private final MutableComponent resourceTitle;
    private Visibility wantedVisibility;

    public TaskCompletedToast(final ResourceKey resource, final long amount) {
        this.resource = resource;
        this.rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        this.resourceTitle = Component.literal(rendering.formatAmount(amount, true))
            .append(" ")
            .append(rendering.getDisplayName(resource));
        this.wantedVisibility = Visibility.HIDE;
    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(final ToastManager manager, final long fullyVisibleForMs) {
        this.wantedVisibility = fullyVisibleForMs >= TIME_VISIBLE * manager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE
            : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final Font font, final long l) {
        graphics.blitSprite(GUI_TEXTURED, SPRITE, 0, 0, width(), height());
        rendering.render(resource, graphics, 8, 8);
        graphics.text(font, TITLE, 8 + 18 + MARGIN, 7, 0xFFFFA500);
        graphics.text(font, resourceTitle, 8 + 18 + MARGIN, 7 + 2 + 9, 0xFFFFFFFF);
    }
}
