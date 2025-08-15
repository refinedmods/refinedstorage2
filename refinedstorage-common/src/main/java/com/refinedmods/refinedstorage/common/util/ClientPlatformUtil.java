package com.refinedmods.refinedstorage.common.util;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.TaskCompletedToast;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewScreen;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

public final class ClientPlatformUtil {
    private static final SystemToast.SystemToastId MESSAGE_TOAST_ID = new SystemToast.SystemToastId();

    private ClientPlatformUtil() {
    }

    @Nullable
    public static Level getClientLevel() { // avoids classloading issues
        return Minecraft.getInstance().level;
    }

    public static void addMessageToast(final Component title, final Component message) {
        final Minecraft minecraft = Minecraft.getInstance();
        final SystemToast toast = SystemToast.multiline(minecraft, MESSAGE_TOAST_ID, title, message);
        minecraft.getToasts().addToast(toast);
    }

    public static void autocraftingPreviewResponseReceived(final UUID id, final Preview preview) {
        if (Minecraft.getInstance().screen instanceof AutocraftingPreviewScreen screen) {
            screen.getMenu().previewResponseReceived(id, preview);
        }
    }

    public static void autocraftingPreviewResponseReceived(final UUID id, final TreePreview preview) {
        if (Minecraft.getInstance().screen instanceof AutocraftingPreviewScreen screen) {
            screen.getMenu().previewResponseReceived(id, preview);
        }
    }

    public static void autocraftingPreviewCancelResponseReceived() {
        if (Minecraft.getInstance().screen instanceof AutocraftingPreviewScreen screen) {
            screen.cancelResponseReceived();
        }
    }

    public static void autocraftingResponseReceived(final UUID id, final boolean success) {
        if (Minecraft.getInstance().screen instanceof AutocraftingPreviewScreen screen) {
            screen.getMenu().responseReceived(id, success);
        }
    }

    public static void autocraftingPreviewMaxAmountResponseReceived(final long maxAmount) {
        if (Minecraft.getInstance().screen instanceof AutocraftingPreviewScreen screen) {
            screen.getMenu().maxAmountResponseReceived(maxAmount);
        }
    }

    public static void openCraftingPreview(final List<ResourceAmount> requests, @Nullable final Object parentScreen) {
        final Minecraft minecraft = Minecraft.getInstance();
        if ((!(parentScreen instanceof Screen) && minecraft.screen == null) || minecraft.player == null) {
            return;
        }
        final Inventory inventory = minecraft.player.getInventory();
        minecraft.setScreen(new AutocraftingPreviewScreen(
            parentScreen instanceof Screen castedParentScreen ? castedParentScreen : minecraft.screen,
            inventory,
            requests.stream().map(AutocraftingRequest::of).toList()
        ));
    }

    public static void autocraftingTaskCompleted(final PlatformResourceKey resource, final long amount) {
        Minecraft.getInstance().getToasts().addToast(new TaskCompletedToast(resource, amount));
    }
}
