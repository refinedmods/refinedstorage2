package com.refinedmods.refinedstorage.common.util;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.autocrafting.TaskCompletedToast;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewScreen;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.List;
import java.util.UUID;

import com.mojang.math.OctahedralGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ClientPlatformUtil {
    private static final SystemToast.SystemToastId MESSAGE_TOAST_ID = new SystemToast.SystemToastId();
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace(
        "container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace(
        "container/slot_highlight_front");

    private ClientPlatformUtil() {
    }

    @Nullable
    public static Level getClientLevel() { // avoids classloading issues
        return Minecraft.getInstance().level;
    }

    public static void addMessageToast(final Component title, final Component message) {
        final Minecraft minecraft = Minecraft.getInstance();
        final SystemToast toast = SystemToast.multiline(minecraft, MESSAGE_TOAST_ID, title, message);
        minecraft.getToastManager().addToast(toast);
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
        Minecraft.getInstance().getToastManager().addToast(new TaskCompletedToast(resource, amount));
    }

    public static void renderSlotHighlightBack(final GuiGraphicsExtractor graphics, final int x, final int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, x - 4, y - 4, 24, 24);
    }

    public static void renderSlotHighlightFront(final GuiGraphicsExtractor graphics, final int x, final int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
    }

    public static BlockModelRotation getRotation(final OrientedDirection direction) {
        return switch (direction) {
            case NORTH -> BlockModelRotation.get(OctahedralGroup.IDENTITY);
            case EAST -> BlockModelRotation.get(OctahedralGroup.ROT_90_Y_NEG);
            case SOUTH -> BlockModelRotation.get(OctahedralGroup.ROT_180_FACE_XZ);
            case WEST -> BlockModelRotation.get(OctahedralGroup.ROT_90_Y_POS);
            case UP_NORTH -> BlockModelRotation.get(OctahedralGroup.ROT_180_EDGE_YZ_NEG);
            case UP_EAST -> BlockModelRotation.get(OctahedralGroup.ROT_120_PPN);
            case UP_SOUTH -> BlockModelRotation.get(OctahedralGroup.ROT_90_X_POS);
            case UP_WEST -> BlockModelRotation.get(OctahedralGroup.ROT_120_PNP);
            case DOWN_NORTH -> BlockModelRotation.get(OctahedralGroup.ROT_90_X_NEG);
            case DOWN_EAST -> BlockModelRotation.get(OctahedralGroup.ROT_120_NNN);
            case DOWN_SOUTH -> BlockModelRotation.get(OctahedralGroup.ROT_180_EDGE_YZ_POS);
            case DOWN_WEST -> BlockModelRotation.get(OctahedralGroup.ROT_120_NPP);
        };
    }

    public static TextureAtlasSprite getFluidSprite(final FluidResource fluidResource) {
        final ModelManager modelManager = Minecraft.getInstance().getModelManager();
        final FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
        final FluidModel fluidModel = fluidStateModelSet.get(fluidResource.fluid().defaultFluidState());
        return getFluidSprite(fluidModel);
    }

    public static TextureAtlasSprite getFluidSprite(final FluidModel fluidModel) {
        final Material.Baked stillMaterial = fluidModel.stillMaterial();
        return stillMaterial.sprite();
    }

    @Nullable
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
