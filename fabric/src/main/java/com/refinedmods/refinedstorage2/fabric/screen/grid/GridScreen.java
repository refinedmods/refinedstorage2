package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.screen.handler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screen.widget.History;
import com.refinedmods.refinedstorage2.fabric.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GridScreen extends HandledScreen<GridScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(RefinedStorage2Mod.ID, "textures/gui/grid.png");

    private static final int TOP_HEIGHT = 19;
    private static final int BOTTOM_HEIGHT = 99;
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();

    private ScrollbarWidget scrollbar;
    private SearchFieldWidget searchField;
    private int visibleRows;

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

        getScreenHandler().addSlots(backgroundHeight - BOTTOM_HEIGHT + 17);

        this.scrollbar = new ScrollbarWidget(client, x + 174, y + 20, 12, (visibleRows * 18) - 2);

        children.add(scrollbar);
        addButton(searchField);
    }

    private int calculateVisibleRows() {
        int screenSpaceAvailable = height - TOP_HEIGHT - BOTTOM_HEIGHT;
        int maxRows = Integer.MAX_VALUE;

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

        GridView view = getScreenHandler().getView();

        int i = 0;
        for (int row = 0; row < visibleRows; ++row) {
            for (int column = 0; column < 9; ++column) {
                int slotX = x + 8 + (column * 18);
                int slotY = y + 20 + (row * 18);

                if (i < view.getStacks().size()) {
                    ItemStack stack = view.getStacks().get(i);

                    setZOffset(100);
                    itemRenderer.zOffset = 100.0F;

                    itemRenderer.renderInGuiWithOverrides(client.player, stack, slotX, slotY);
                    renderAmount(matrices, slotX, slotY, String.valueOf(stack.getCount()), Formatting.WHITE.getColorValue());

                    setZOffset(0);
                    itemRenderer.zOffset = 0.0F;
                }

                if (mouseX >= slotX && mouseY >= slotY && mouseX <= slotX + 16 && mouseY <= slotY + 16) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.colorMask(true, true, true, false);
                    fillGradient(matrices, slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.enableDepthTest();
                }

                i++;
            }
        }
    }

    private void renderAmount(MatrixStack matrixStack, int x, int y, String amount, int color) {
        boolean large = this.client.forcesUnicodeFont(); /* TODO RS large font config */

        matrixStack.push();
        matrixStack.translate(x, y, 300);

        if (!large) {
            matrixStack.scale(0.5F, 0.5F, 1);
        }

        textRenderer.drawWithShadow(matrixStack, amount, (large ? 16 : 30) - textRenderer.getWidth(amount), large ? 8 : 22, color);

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

        if (isOverStorageArea((int) mouseX, (int) mouseY) && !cursorStack.isEmpty() && (clickedButton == 0 || clickedButton == 1)) {
            PacketUtil.sendToServer(GridInsertFromCursorPacket.ID, buf -> buf.writeBoolean(clickedButton == 1));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
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
        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
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

        return super.keyPressed(key, scanCode, modifiers);
    }
}
