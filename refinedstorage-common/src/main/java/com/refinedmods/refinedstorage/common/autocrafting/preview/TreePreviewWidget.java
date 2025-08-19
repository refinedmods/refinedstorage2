package com.refinedmods.refinedstorage.common.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.TreeLayout;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.util.DefaultConfiguration;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.util.DefaultTreeForTreeLayout;
import com.refinedmods.refinedstorage.common.repackage.org.abego.treelayout.util.FixedNodeExtentProvider;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class TreePreviewWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = createIdentifier("autocrafting_preview/tree_background");
    private static final ResourceLocation NODE_BACKGROUND = createIdentifier("autocrafting_preview/tree_node");
    private static final ResourceLocation NODE_BACKGROUND_HOVER =
        createIdentifier("autocrafting_preview/tree_node_hover");
    private static final ResourceLocation CRAFTING_NODE_BACKGROUND =
        createIdentifier("autocrafting_preview/tree_node_crafting");
    private static final ResourceLocation CRAFTING_NODE_BACKGROUND_HOVER =
        createIdentifier("autocrafting_preview/tree_node_crafting_hover");
    private static final ResourceLocation MISSING_NODE_BACKGROUND =
        createIdentifier("autocrafting_preview/tree_node_missing");
    private static final ResourceLocation MISSING_NODE_BACKGROUND_HOVER =
        createIdentifier("autocrafting_preview/tree_node_missing_hover");

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 4.0;
    private static final double SMOOTH_FACTOR = 0.15;
    private static final double[] ZOOM_LEVELS = new double[] {0.5, 1.0, 2.0, 4.0};
    private static final double DRAG_SMOOTH_FACTOR = 0.15;
    private static final int LINE_COLOR = 0xFF818181;
    private static final int INACTIVE_LINE_COLOR = 0x80818181;
    private static final int NODE_SIZE = 26;

    private final Screen screen;
    private final Set<TreePreviewNode> activeNodes = new HashSet<>();

    @Nullable
    private TreeLayout<TreePreviewNode> tree;
    @Nullable
    private TreePreviewNode hoveredNode;

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

    void update(@Nullable final TreePreview preview) {
        if (preview == null || preview.rootNode() == null) {
            this.tree = null;
            return;
        }
        final DefaultTreeForTreeLayout<TreePreviewNode> treeContents =
            new DefaultTreeForTreeLayout<>(preview.rootNode());
        addChildren(treeContents, preview.rootNode());
        this.tree = new TreeLayout<>(
            treeContents,
            new FixedNodeExtentProvider<>(NODE_SIZE, NODE_SIZE),
            new DefaultConfiguration<>(15, 10)
        );
        ensureThatTheTreeIsCentered();
    }

    boolean hasContents() {
        return tree != null;
    }

    private void ensureThatTheTreeIsCentered() {
        if (tree == null) {
            return;
        }
        final Rectangle2D bounds = this.tree.getBounds();
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
        if (!visible) {
            return;
        }
        renderBackground(graphics);
        if (tree == null) {
            return;
        }
        zoom += (targetZoom - zoom) * SMOOTH_FACTOR;
        translateX += (targetTranslateX - translateX) * DRAG_SMOOTH_FACTOR;
        translateY += (targetTranslateY - translateY) * DRAG_SMOOTH_FACTOR;
        graphics.pose().pushPose();
        graphics.pose().translate(translateX, translateY, 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1.0f);
        renderEdges(graphics, tree.getTree().getRoot());
        if (!renderNode(graphics, tree.getTree().getRoot(), mouseX, mouseY)) {
            hoveredNode = null;
            activeNodes.clear();
        }
        graphics.pose().popPose();
    }

    private void renderBackground(final GuiGraphics graphics) {
        graphics.blitSprite(BACKGROUND, 0, 0, width, height);
    }

    private boolean renderNode(final GuiGraphics graphics, final TreePreviewNode node,
                               final int mouseX, final int mouseY) {
        if (tree == null) {
            return false;
        }
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            node.getResource().getClass()
        );
        final var bounds = tree.getNodeBounds().get(node);
        boolean anyHovering = false;
        if (isInView((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height)) {
            anyHovering = renderNode(graphics, node, bounds, rendering, mouseX, mouseY);
        }
        for (final TreePreviewNode child : node.getChildren()) {
            if (renderNode(graphics, child, mouseX, mouseY)) {
                anyHovering = true;
            }
        }
        return anyHovering;
    }

    private boolean renderNode(final GuiGraphics graphics, final TreePreviewNode node, final Rectangle2D.Double bounds,
                               final ResourceRendering rendering, final double mouseX, final double mouseY) {
        final double worldMouseX = (mouseX - getX() - translateX) / zoom;
        final double worldMouseY = (mouseY - getY() - translateY) / zoom;
        final boolean hovering = worldMouseX >= bounds.x + 1
            && worldMouseX <= bounds.x + bounds.width - 1
            && worldMouseY >= bounds.y + 1
            && worldMouseY <= bounds.y + bounds.height - 1;
        if (hovering) {
            if (hoveredNode != node) {
                hoveredNode = node;
                activeNodes.clear();
                calculateActiveNodes(hoveredNode);
            }
            screen.setTooltipForNextRenderPass(getTooltip(node, rendering));
        }
        final ResourceLocation background = getNodeBackground(node, hovering);
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
        final boolean inactive = !activeNodes.isEmpty() && !activeNodes.contains(node);
        if (inactive) {
            graphics.fill((int) bounds.x + 2, (int) bounds.y + 2,
                (int) bounds.x + (int) bounds.width - 2, (int) bounds.y + (int) bounds.height - 2, 400, 0x80000000);
        }
        return hovering;
    }

    private void calculateActiveNodes(final TreePreviewNode node) {
        activeNodes.add(node);
        node.getChildren().forEach(this::calculateActiveNodes);
    }

    private static ResourceLocation getNodeBackground(final TreePreviewNode node, final boolean hovering) {
        if (node.getMissing() > 0) {
            return hovering ? MISSING_NODE_BACKGROUND_HOVER : MISSING_NODE_BACKGROUND;
        }
        if (node.getToCraft() > 0) {
            return hovering ? CRAFTING_NODE_BACKGROUND_HOVER : CRAFTING_NODE_BACKGROUND;
        }
        return hovering ? NODE_BACKGROUND_HOVER : NODE_BACKGROUND;
    }

    private List<FormattedCharSequence> getTooltip(final TreePreviewNode node,
                                                   final ResourceRendering resourceRendering) {
        final List<FormattedCharSequence> tooltip = new ArrayList<>(
            resourceRendering.getTooltip(node.getResource()).stream().map(Component::getVisualOrderText).toList());
        if (node.getAvailable() > 0) {
            tooltip.add(createTranslation("gui", "autocrafting_preview.available",
                resourceRendering.formatAmount(node.getAvailable())).getVisualOrderText());
        }
        if (node.getToCraft() > 0) {
            tooltip.add(createTranslation("gui", "autocrafting_preview.to_craft",
                resourceRendering.formatAmount(node.getToCraft())).getVisualOrderText());
        }
        if (node.getMissing() > 0) {
            tooltip.add(createTranslation("gui", "autocrafting_preview.missing",
                resourceRendering.formatAmount(node.getMissing())).getVisualOrderText());
        }
        return tooltip;
    }

    private void renderEdges(final GuiGraphics graphics, final TreePreviewNode node) {
        if (tree == null || tree.getTree().isLeaf(node)) {
            return;
        }
        final var bounds1 = tree.getNodeBounds().get(node);
        final int x1 = (int) bounds1.getCenterX();
        final int y1 = (int) bounds1.getCenterY();
        for (final TreePreviewNode child : node.getChildren()) {
            if (tree == null) {
                return;
            }
            final var bounds2 = tree.getNodeBounds().get(child);
            final int x2 = (int) bounds2.getCenterX();
            final int y2 = (int) bounds2.getCenterY();
            if (isInView(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1))) {
                final boolean active = !activeNodes.isEmpty()
                    && (!activeNodes.contains(child) || !activeNodes.contains(node));
                final int lineColor = active ? INACTIVE_LINE_COLOR : LINE_COLOR;
                drawLine(graphics, x1, y1, x2, y2, lineColor);
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
