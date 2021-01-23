package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage2.core.grid.*;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.core.util.History;
import com.refinedmods.refinedstorage2.core.util.Quantities;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Config;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridChangeSettingPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.screen.handler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.util.LastModifiedUtil;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GridScreen extends BaseScreen<GridScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(RefinedStorage2Mod.ID, "textures/gui/grid.png");

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final int COLUMNS = 9;
    private static final GridQueryParser<ItemStack> QUERY_PARSER = new GridQueryParser<>();

    private ScrollbarWidget scrollbar;
    private SearchFieldWidget searchField;
    private int visibleRows;
    private int gridSlotNumber;

    public GridScreen(GridScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.titleX = 7;
        this.titleY = 7;
        this.playerInventoryTitleX = 7;
        this.playerInventoryTitleY = 75;
        this.backgroundWidth = 227;
        this.backgroundHeight = 176;
    }

    @Override
    protected void init() {
        this.visibleRows = calculateVisibleRows();
        this.backgroundHeight = TOP_HEIGHT + (visibleRows * 18) + BOTTOM_HEIGHT;
        this.playerInventoryTitleY = backgroundHeight - BOTTOM_HEIGHT + 4;

        super.init();

        if (searchField == null) {
            searchField = new SearchFieldWidget(textRenderer, x + 80 + 1, y + 6 + 1, 88 - 6, new History(SEARCH_FIELD_HISTORY));
        } else {
            searchField.x = x + 80 + 1;
            searchField.y = y + 6 + 1;
        }
        searchField.setChangedListener(text -> {
            try {
                getScreenHandler().getItemView().setFilter(QUERY_PARSER.parse(text));
            } catch (GridQueryParserException e) {
                getScreenHandler().getItemView().setFilter(stack -> false);
            }
            getScreenHandler().getItemView().sort();
        });

        getScreenHandler().addSlots(backgroundHeight - BOTTOM_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(client, x + 174, y + 20, 12, (visibleRows * 18) - 2);
        this.getScreenHandler().getItemView().setListener(this::updateScrollbar);
        updateScrollbar();

        children.add(scrollbar);
        addButton(searchField);

        addSideButton(new SideButtonWidget(btn -> {
            GridSortingDirection sortingDirection = getScreenHandler().getItemView().getSortingDirection().toggle();
            getScreenHandler().getItemView().setSortingDirection(sortingDirection);
            getScreenHandler().getItemView().sort();

            PacketUtil.sendToServer(GridChangeSettingPacket.ID, buf -> GridChangeSettingPacket.writeSortingDirection(
                buf,
                sortingDirection == GridSortingDirection.ASCENDING ? GridBlockEntity.SORTING_ASCENDING : GridBlockEntity.SORTING_DESCENDING
            ));
        }) {
            @Override
            protected int getXTexture() {
                return getScreenHandler().getItemView().getSortingDirection() == GridSortingDirection.ASCENDING ? 0 : 16;
            }

            @Override
            protected int getYTexture() {
                return 16;
            }

            @Override
            public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
                List<Text> lines = new ArrayList<>();
                lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.direction"));
                lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.direction." + getScreenHandler().getItemView().getSortingDirection().toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
                renderTooltip(matrixStack, lines, mouseX, mouseY);
            }
        });

        addSideButton(new SideButtonWidget(btn -> {

        }) {
            @Override
            protected int getXTexture() {
                return;
            }

            @Override
            protected int getYTexture() {
                return 0;
            }

            @Override
            public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int i, int j) {

            }
        });
    }

    private void updateScrollbar() {
        int rows = (int) Math.ceil((float) getScreenHandler().getItemView().getStacks().size() / (float) COLUMNS);

        scrollbar.setEnabled(rows > visibleRows);
        scrollbar.setMaxOffset(rows - visibleRows);
    }

    private int calculateVisibleRows() {
        int screenSpaceAvailable = height - TOP_HEIGHT - BOTTOM_HEIGHT;
        int maxRows = RefinedStorage2Config.get().getGrid().getMaxRowsStretch();

        return Math.max(3, Math.min((screenSpaceAvailable / 18) - 3, maxRows));
    }

    private boolean isOverStorageArea(int mouseX, int mouseY) {
        mouseX -= x;
        mouseY -= y;

        return mouseX >= 7 && mouseY >= 19
            && mouseX <= 168 && mouseY <= 19 + (visibleRows * 18);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ScreenUtil.drawVersionInformation(matrices, textRenderer, delta);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x, y, 0, 0, backgroundWidth - 34, TOP_HEIGHT);

        for (int row = 0; row < visibleRows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == visibleRows - 1) {
                textureY = 55;
            }

            drawTexture(matrices, x, y + TOP_HEIGHT + (18 * row), 0, textureY, backgroundWidth - 34, 18);
        }

        drawTexture(matrices, x, y + TOP_HEIGHT + (18 * visibleRows), 0, 73, backgroundWidth - 34, BOTTOM_HEIGHT);

        gridSlotNumber = -1;

        int idx = scrollbar.getOffset() * COLUMNS;
        for (int row = 0; row < visibleRows; ++row) {
            for (int column = 0; column < COLUMNS; ++column) {
                renderGridSlot(matrices, mouseX, mouseY, x, y, idx, row, column);
                idx++;
            }
        }
    }

    private void renderGridSlot(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int idx, int row, int column) {
        GridView<ItemStack> view = getScreenHandler().getItemView();

        int slotX = x + 8 + (column * 18);
        int slotY = y + 20 + (row * 18);

        GridStack<ItemStack> stack = null;
        if (idx < view.getStacks().size()) {
            stack = view.getStacks().get(idx);

            setZOffset(100);
            itemRenderer.zOffset = 100.0F;

            itemRenderer.renderInGuiWithOverrides(client.player, stack.getStack(), slotX, slotY);

            String text = stack.isZeroed() ? "0" : String.valueOf(stack.getCount());
            Integer color = stack.isZeroed() ? Formatting.RED.getColorValue() : Formatting.WHITE.getColorValue();

            renderAmount(matrices, slotX, slotY, text, color);

            setZOffset(0);
            itemRenderer.zOffset = 0.0F;
        }

        if (mouseX >= slotX && mouseY >= slotY && mouseX <= slotX + 16 && mouseY <= slotY + 16) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();

            if (stack != null) {
                gridSlotNumber = idx;

                if (!RefinedStorage2Config.get().getGrid().isDetailedTooltip()) {
                    renderTooltip(matrices, stack.getStack(), mouseX, mouseY);
                } else {
                    List<OrderedText> lines = Lists.transform(getTooltipFromItem(stack.getStack()), Text::asOrderedText);
                    List<OrderedText> smallLines = new ArrayList<>();
                    smallLines.add(new TranslatableText("misc.refinedstorage2.total", stack.isZeroed() ? "0" : Quantities.format(stack.getCount())).formatted(Formatting.GRAY).asOrderedText());

                    view.getTrackerEntry(stack.getStack()).ifPresent(entry -> smallLines.add(LastModifiedUtil.getText(entry.getTime(), entry.getName()).formatted(Formatting.GRAY).asOrderedText()));

                    renderTooltipWithSmallText(matrices, lines, smallLines, mouseX, mouseY);
                }
            }
        }
    }

    private void renderAmount(MatrixStack matrixStack, int x, int y, String amount, int color) {
        boolean large = this.client.forcesUnicodeFont() || RefinedStorage2Config.get().getGrid().isLargeFont();

        matrixStack.push();
        matrixStack.translate(x, y, 300);

        if (!large) {
            matrixStack.scale(0.5F, 0.5F, 1);
        }

        textRenderer.drawWithShadow(matrixStack, amount, (large ? 16 : 30) - textRenderer.getWidth(amount), large ? 8 : 22, color);

        matrixStack.pop();
    }

    private void renderTooltipWithSmallText(MatrixStack matrixStack, List<? extends OrderedText> lines, List<? extends OrderedText> smallLines, int x, int y) {
        if (lines.isEmpty()) {
            return;
        }

        float smallTextScale = client.forcesUnicodeFont() ? 1F : 0.7F;

        int tooltipWidth = 0;

        for (OrderedText text : lines) {
            int textWidth = textRenderer.getWidth(text);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }

        for (OrderedText text : smallLines) {
            int textWidth = (int) (textRenderer.getWidth(text) * smallTextScale);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }

        int tooltipX = x + 12;
        int tooltipY = y - 12;
        int tooltipHeight = 8;

        if (lines.size() > 1) {
            tooltipHeight += 2 + (lines.size() - 1) * 10;
        }

        tooltipHeight += smallLines.size() * 10;

        if (tooltipX + tooltipWidth > width) {
            tooltipX -= 28 + tooltipWidth;
        }

        if (tooltipY + tooltipHeight + 6 > height) {
            tooltipY = height - tooltipHeight - 6;
        }

        matrixStack.push();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, 1347420415, 1347420415);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrixStack.translate(0.0D, 0.0D, 400.0D);

        for (int i = 0; i < lines.size(); ++i) {
            OrderedText text = lines.get(i);
            if (text != null) {
                textRenderer.draw(text, (float) tooltipX, (float) tooltipY, -1, true, matrix4f, immediate, false, 0, 15728880);
            }

            if (i == 0) {
                tooltipY += 2;
            }

            tooltipY += 10;
        }

        for (int i = 0; i < smallLines.size(); ++i) {
            matrixStack.push();
            matrixStack.scale(smallTextScale, smallTextScale, 1);

            textRenderer.draw(smallLines.get(i), (float) tooltipX / smallTextScale, (float) tooltipY / smallTextScale, -1, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
            matrixStack.pop();

            tooltipY += 9;
        }

        immediate.draw();
        matrixStack.pop();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);

        scrollbar.render(matrices, mouseX, mouseY, partialTicks);
        searchField.render(matrices, 0, 0, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        ItemStack cursorStack = playerInventory.getCursorStack();

        if (!getScreenHandler().getItemView().getStacks().isEmpty() && gridSlotNumber >= 0 && cursorStack.isEmpty()) {
            GridStack<ItemStack> stack = getScreenHandler().getItemView().getStacks().get(gridSlotNumber);

            PacketUtil.sendToServer(GridExtractPacket.ID, buf -> {
                PacketUtil.writeItemStackWithoutCount(buf, stack.getStack());
                GridExtractPacket.writeMode(buf, getExtractMode(clickedButton));
            });

            return true;
        }

        if (isOverStorageArea((int) mouseX, (int) mouseY) && !cursorStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            PacketUtil.sendToServer(GridInsertFromCursorPacket.ID, buf -> buf.writeBoolean(clickedButton == 1));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private GridExtractMode getExtractMode(int clickedButton) {
        if (clickedButton == 1) {
            return GridExtractMode.CURSOR_HALF;
        }
        if (hasShiftDown()) {
            return GridExtractMode.PLAYER_INVENTORY_STACK;
        }
        return GridExtractMode.CURSOR_STACK;
    }

    @Override
    public void mouseMoved(double mx, double my) {
        scrollbar.mouseMoved(mx, my);

        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return scrollbar.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        boolean up = delta > 0;
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();

        if (shift || ctrl) {
            if (isOverStorageArea((int) x, (int) y) && gridSlotNumber >= 0) {
                getScreenHandler().getItemView().setPreventSorting(true);

                ItemStack stack = getScreenHandler().getItemView().getStacks().get(gridSlotNumber).getStack();
                PacketUtil.sendToServer(GridScrollPacket.ID, buf -> {
                    PacketUtil.writeItemStackWithoutCount(buf, stack);
                    GridScrollPacket.writeMode(buf, getScrollInGridMode(shift, up));
                    buf.writeInt(playerInventory.getSlotWithStack(stack));
                });
            } else if (focusedSlot != null && focusedSlot.hasStack()) {
                getScreenHandler().getItemView().setPreventSorting(true);

                PacketUtil.sendToServer(GridScrollPacket.ID, buf -> {
                    PacketUtil.writeItemStackWithoutCount(buf, focusedSlot.getStack());
                    GridScrollPacket.writeMode(buf, getScrollInGridMode(shift, up));
                    buf.writeInt(((SlotAccessor) focusedSlot).getIndex());
                });
            }
        }

        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    private GridScrollMode getScrollInGridMode(boolean shift, boolean up) {
        if (up) {
            return shift ? GridScrollMode.INVENTORY_TO_GRID_STACK : GridScrollMode.INVENTORY_TO_GRID_SINGLE_STACK;
        } else {
            return shift ? GridScrollMode.GRID_TO_INVENTORY_STACK : GridScrollMode.GRID_TO_INVENTORY_SINGLE_STACK;
        }
    }

    @Override
    public boolean charTyped(char unknown1, int unknown2) {
        return searchField.charTyped(unknown1, unknown2) || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchField.keyPressed(key, scanCode, modifiers) || searchField.isActive()) {
            return true;
        }

        if (hasShiftDown() && RefinedStorage2Config.get().getGrid().isPreventSortingWhileShiftIsDown()) {
            getScreenHandler().getItemView().setPreventSorting(true);
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (getScreenHandler().getItemView().isPreventSorting()) {
            getScreenHandler().getItemView().setPreventSorting(false);
            getScreenHandler().getItemView().sort();
        }

        return super.keyReleased(key, scanCode, modifiers);
    }
}
