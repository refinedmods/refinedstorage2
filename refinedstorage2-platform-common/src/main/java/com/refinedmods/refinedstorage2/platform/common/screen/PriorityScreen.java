package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.PriorityAccessor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class PriorityScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/priority.png");

    private static final int ACTION_BUTTON_WIDTH = 50;
    private static final int ACTION_BUTTON_X = 107;
    private static final int ACTION_BUTTON_Y = 18;

    private static final int AMOUNT_X = 19;
    private static final int AMOUNT_Y = 48;

    private static final MutableComponent PRIORITY_TEXT = createTranslation("gui", "priority");
    private static final MutableComponent SET_TEXT = createTranslation("gui", "priority.set");
    private static final MutableComponent RESET_TEXT = createTranslation("gui", "priority.reset");
    private static final MutableComponent CANCEL_TEXT = Component.translatable("gui.cancel");

    private static final int[] INCREMENTS_TOP = {1, 5, 10};
    private static final int[] INCREMENTS_BOTTOM = {-1, -5, -10};
    private static final int INCREMENT_BUTTON_WIDTH = 30;
    private static final int INCREMENT_BUTTON_X = 7;
    private static final int INCREMENT_BUTTON_TOP_Y = 20;
    private static final int INCREMENT_BUTTON_BOTTOM_Y = 64;

    private final Screen parent;
    private final PriorityAccessor priorityAccessor;

    private EditBox amountField;

    public PriorityScreen(PriorityAccessor priorityAccessor, Screen parent, Inventory playerInventory) {
        super(new DummyContainerMenu(), playerInventory, PriorityScreen.PRIORITY_TEXT);

        this.parent = parent;
        this.priorityAccessor = priorityAccessor;

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.imageWidth = 164;
        this.imageHeight = 92;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new Button(leftPos + ACTION_BUTTON_X, topPos + ACTION_BUTTON_Y, ACTION_BUTTON_WIDTH, 20, RESET_TEXT, btn -> reset()));
        addRenderableWidget(new Button(leftPos + ACTION_BUTTON_X, topPos + ACTION_BUTTON_Y + 24, ACTION_BUTTON_WIDTH, 20, SET_TEXT, btn -> ok()));
        addRenderableWidget(new Button(leftPos + ACTION_BUTTON_X, topPos + ACTION_BUTTON_Y + 48, ACTION_BUTTON_WIDTH, 20, CANCEL_TEXT, btn -> close()));

        amountField = new EditBox(font, leftPos + AMOUNT_X, topPos + AMOUNT_Y, 69 - 6, font.lineHeight, Component.empty());
        amountField.setBordered(false);
        amountField.setValue(String.valueOf(priorityAccessor.getPriority()));
        amountField.setVisible(true);
        amountField.setCanLoseFocus(false);
        amountField.setFocus(true);

        addRenderableWidget(amountField);

        addIncrementButtons(INCREMENTS_TOP, leftPos + INCREMENT_BUTTON_X, topPos + INCREMENT_BUTTON_TOP_Y);
        addIncrementButtons(INCREMENTS_BOTTOM, leftPos + INCREMENT_BUTTON_X, topPos + INCREMENT_BUTTON_BOTTOM_Y);
    }

    private void addIncrementButtons(int[] increments, int x, int y) {
        for (int increment : increments) {
            Component text = Component.literal((increment > 0 ? "+" : "") + increment);

            addRenderableWidget(new Button(x, y, INCREMENT_BUTTON_WIDTH, 20, text, btn -> changeAmount(increment)));

            x += INCREMENT_BUTTON_WIDTH + 3;
        }
    }

    private void changeAmount(int delta) {
        int oldAmount = getAmount();
        int newAmount = oldAmount + delta;
        amountField.setValue(String.valueOf(newAmount));
    }

    private int getAmount() {
        try {
            return Integer.parseInt(amountField.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        if (delta > 0) {
            changeAmount(1);
        } else {
            changeAmount(-1);
        }
        return super.mouseScrolled(x, y, delta);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, title, titleLabelX, titleLabelY, 4210752);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && amountField.isFocused()) {
            ok();
            return true;
        }

        if (amountField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    private void reset() {
        amountField.setValue("0");
    }

    private void ok() {
        priorityAccessor.setPriority(getAmount());
        close();
    }

    private void close() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static class DummyContainerMenu extends AbstractContainerMenu {
        protected DummyContainerMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
