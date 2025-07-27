package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.TreeLayout;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.util.DefaultConfiguration;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.util.DefaultTreeForTreeLayout;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;

import java.awt.geom.Rectangle2D;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class TreePreviewWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = createIdentifier("autocrafting_preview/tree_background");
    private static final ResourceLocation NODE_BACKGROUND = createIdentifier("autocrafting_preview/tree_node");
    private static final ResourceLocation NODE_BACKGROUND_HOVER =
        createIdentifier("autocrafting_preview/tree_node_hover");
    private static final ResourceLocation MISSING_NODE_BACKGROUND =
        createIdentifier("autocrafting_preview/tree_node_missing");
    private static final ResourceLocation MISSING_NODE_BACKGROUND_HOVER =
        createIdentifier("autocrafting_preview/tree_node_missing_hover");

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 4.0;
    private static final double SMOOTH_FACTOR = 0.15;
    private static final double[] ZOOM_LEVELS = new double[] {0.5, 1.0, 2.0, 4.0};
    private static final double DRAG_SMOOTH_FACTOR = 0.15;

    private final Screen screen;

    @Nullable
    private TreeLayout<TreePreviewNode> treeLayout;
    @Nullable
    private TreePreview treePreview;

    private double zoom = 1.0;
    private double targetZoom = 1.0;

    private double translateX = 0;
    private double translateY = 0;
    private double targetTranslateX = 0;
    private double targetTranslateY = 0;

    TreePreviewWidget(final Screen screen, final int x, final int y, final int width, final int height) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    void setPreview(@Nullable final TreePreview preview) {
        this.treePreview = preview;
        if (preview == null || preview.rootNode() == null) {
            this.treeLayout = null;
            this.active = false;
            return;
        }
        final DefaultTreeForTreeLayout<TreePreviewNode> treeContents =
            new DefaultTreeForTreeLayout<>(preview.rootNode());
        addChildren(treeContents, preview.rootNode());
        this.treeLayout = new TreeLayout<>(
            treeContents,
            new TreePreviewNodeExtentProvider(),
            new DefaultConfiguration<>(15, 10)
        );
        this.active = true;
        ensureThatTheTreeIsCentered();
    }

    private void ensureThatTheTreeIsCentered() {
        if (treeLayout == null) {
            return;
        }
        final Rectangle2D bounds = this.treeLayout.getBounds();
        final double scaleX = this.width / bounds.getWidth();
        final double scaleY = this.height / bounds.getHeight();
        final double scale = Math.min(scaleX, scaleY);
        if (scale < 1.0) {
            this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, scale));
            this.targetZoom = this.zoom;
        } else {
            this.zoom = 1.0;
            this.targetZoom = 1.0;
        }
        final double centerX = bounds.getX() + bounds.getWidth() / 2;
        final double centerY = bounds.getY() + bounds.getHeight() / 2;
        this.translateX = (this.width / 2.0) - centerX * this.zoom;
        this.translateY = (this.height / 2.0) - centerY * this.zoom;
        this.targetTranslateX = translateX;
        this.targetTranslateY = translateY;
    }

    private static void addChildren(final DefaultTreeForTreeLayout<TreePreviewNode> treeContents,
                                    final TreePreviewNode parent) {
        for (final TreePreviewNode child : parent.getChildren()) {
            treeContents.addChild(parent, child);
            addChildren(treeContents, child);
        }
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        if (!visible || treePreview == null || treePreview.rootNode() == null) {
            return;
        }
        zoom += (targetZoom - zoom) * SMOOTH_FACTOR;
        translateX += (targetTranslateX - translateX) * DRAG_SMOOTH_FACTOR;
        translateY += (targetTranslateY - translateY) * DRAG_SMOOTH_FACTOR;
        renderBackground(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(translateX, translateY, 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1.0f);
        renderEdges(graphics, treePreview.rootNode());
        renderNode(graphics, treePreview.rootNode(), mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void renderBackground(final GuiGraphics graphics) {
        graphics.blitSprite(BACKGROUND, 0, 0, width, height);
    }

    private void renderNode(final GuiGraphics graphics, final TreePreviewNode node,
                            final int mouseX, final int mouseY) {
        if (treeLayout == null) {
            return;
        }
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            node.getResource().getClass()
        );
        final var bounds = treeLayout.getNodeBounds().get(node);
        if (isInView((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height)) {
            renderNode(graphics, node, bounds, rendering, mouseX, mouseY);
        }
        for (final TreePreviewNode child : node.getChildren()) {
            renderNode(graphics, child, mouseX, mouseY);
        }
    }

    private void renderNode(final GuiGraphics graphics, final TreePreviewNode node, final Rectangle2D.Double bounds,
                            final ResourceRendering rendering, final double mouseX, final double mouseY) {
        final double worldMouseX = (mouseX - getX() - translateX) / zoom;
        final double worldMouseY = (mouseY - getY() - translateY) / zoom;
        final boolean hovering = worldMouseX >= bounds.x + 1
            && worldMouseX <= bounds.x + bounds.width - 1
            && worldMouseY >= bounds.y + 1
            && worldMouseY <= bounds.y + bounds.height - 1;
        if (hovering) {
            screen.setTooltipForNextRenderPass(rendering.getTooltip(node.getResource()).stream()
                .map(Component::getVisualOrderText)
                .toList());
        }
        final ResourceLocation background = node.getMissing() > 0
            ? (hovering ? MISSING_NODE_BACKGROUND_HOVER : MISSING_NODE_BACKGROUND)
            : (hovering ? NODE_BACKGROUND_HOVER : NODE_BACKGROUND);
        graphics.blitSprite(background, (int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height);
        rendering.render(node.getResource(), graphics, (int) bounds.x + 5, (int) bounds.y + 5);
        final boolean large = Minecraft.getInstance().isEnforceUnicode()
            || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
        ResourceSlotRendering.renderAmount(
            graphics,
            (int) bounds.x + 5,
            (int) bounds.y + 5,
            rendering.formatAmount(node.getAmount(), true),
            0xFFFFFF,
            large
        );
    }

    private void renderEdges(final GuiGraphics graphics, final TreePreviewNode node) {
        if (treeLayout == null || treeLayout.getTree().isLeaf(node)) {
            return;
        }
        final var bounds1 = treeLayout.getNodeBounds().get(node);
        final int x1 = (int) bounds1.getCenterX();
        final int y1 = (int) bounds1.getCenterY();
        for (final TreePreviewNode child : node.getChildren()) {
            if (treeLayout == null) {
                return;
            }
            final var bounds2 = treeLayout.getNodeBounds().get(child);
            final int x2 = (int) bounds2.getCenterX();
            final int y2 = (int) bounds2.getCenterY();
            if (isInView(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1))) {
                drawLine(graphics, x1, y1, x2, y2, 0xFF818181);
            }
            renderEdges(graphics, child);
        }
    }

    private void drawLine(final GuiGraphics graphics, final int x1, final int y1, final int x2, final int y2,
                          final int color) {
        final int dx = Math.abs(x2 - x1);
        final int dy = Math.abs(y2 - y1);
        final int sx = x1 < x2 ? 1 : -1;
        final int sy = y1 < y2 ? 1 : -1;
        int xx = x1;
        int yy = y1;
        int err = dx - dy;
        while (true) {
            graphics.hLine(xx, xx, yy, color);
            if (xx == x2 && yy == y2) {
                break;
            }
            final int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                xx += sx;
            }
            if (e2 < dx) {
                err += dx;
                yy += sy;
            }
        }
    }

    private boolean isInView(final double x, final double y, final int width, final int height) {
        final double screenX = x * zoom + translateX;
        final double screenY = y * zoom + translateY;
        final double screenWidth = width * zoom;
        final double screenHeight = height * zoom;
        return screenX + screenWidth >= 0
            && screenY + screenHeight >= 0
            && screenX <= this.width
            && screenY <= this.height;
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (scrollY != 0.0) {
            final double prevZoom = targetZoom;
            int index = getClosestZoomLevelIndex(targetZoom);
            index = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, index + (scrollY > 0 ? 1 : -1)));
            targetZoom = ZOOM_LEVELS[index];
            zoomInTheDirectionOfTheMouseCursor(mouseX, mouseY, prevZoom);
        }
        return true;
    }

    private int getClosestZoomLevelIndex(final double scrollY) {
        int closest = 0;
        for (int i = 1; i < ZOOM_LEVELS.length; i++) {
            if (Math.abs(ZOOM_LEVELS[i] - scrollY) < Math.abs(ZOOM_LEVELS[closest] - scrollY)) {
                closest = i;
            }
        }
        return closest;
    }

    private void zoomInTheDirectionOfTheMouseCursor(final double mouseX, final double mouseY, final double prevZoom) {
        final double scaleChange = targetZoom / prevZoom;
        final double worldMouseX = (mouseX - getX() - translateX) / zoom;
        final double worldMouseY = (mouseY - getY() - translateY) / zoom;
        targetTranslateX -= (worldMouseX * (scaleChange - 1)) * zoom;
        targetTranslateY -= (worldMouseY * (scaleChange - 1)) * zoom;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX,
                                final double dragY) {
        targetTranslateX += dragX;
        targetTranslateY += dragY;
        return true;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }
}
