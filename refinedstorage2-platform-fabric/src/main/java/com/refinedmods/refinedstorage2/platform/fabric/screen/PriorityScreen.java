package com.refinedmods.refinedstorage2.platform.fabric.screen;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.PriorityAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.util.ScreenUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

public class PriorityScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final ResourceLocation TEXTURE = Rs2Mod.createIdentifier("textures/gui/priority.png");

    private static final int ACTION_BUTTON_WIDTH = 50;
    private static final int ACTION_BUTTON_X = 107;
    private static final int ACTION_BUTTON_Y = 18;

    private static final int AMOUNT_X = 19;
    private static final int AMOUNT_Y = 48;

    private static final TranslatableComponent PRIORITY_TEXT = Rs2Mod.createTranslation("gui", "priority");
    private static final TranslatableComponent SET_TEXT = Rs2Mod.createTranslation("gui", "priority.set");
    private static final TranslatableComponent RESET_TEXT = Rs2Mod.createTranslation("gui", "priority.reset");
    private static final TranslatableComponent CANCEL_TEXT = new TranslatableComponent("gui.cancel");

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
        super(new DummyScreenHandler(), playerInventory, PriorityScreen.PRIORITY_TEXT);

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

        amountField = new EditBox(font, leftPos + AMOUNT_X, topPos + AMOUNT_Y, 69 - 6, font.lineHeight, new TextComponent(""));
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
            Component text = new TextComponent((increment > 0 ? "+" : "") + increment);

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
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        ScreenUtil.drawVersionInformation(matrices, font);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        font.draw(matrices, title, titleLabelX, titleLabelY, 4210752);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);
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

    private static class DummyScreenHandler extends AbstractContainerMenu {
        protected DummyScreenHandler() {
            super(null, 0);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
