package com.refinedmods.refinedstorage2.fabric.screen;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screenhandler.PriorityAccessor;
import com.refinedmods.refinedstorage2.fabric.util.ScreenUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class PriorityScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/priority.png");

    private static final int ACTION_BUTTON_WIDTH = 50;
    private static final int ACTION_BUTTON_X = 107;
    private static final int ACTION_BUTTON_Y = 18;

    private static final int AMOUNT_X = 19;
    private static final int AMOUNT_Y = 48;

    private static final TranslatableText PRIORITY_TEXT = Rs2Mod.createTranslation("gui", "priority");
    private static final TranslatableText SET_TEXT = Rs2Mod.createTranslation("gui", "priority.set");
    private static final TranslatableText RESET_TEXT = Rs2Mod.createTranslation("gui", "priority.reset");
    private static final TranslatableText CANCEL_TEXT = new TranslatableText("gui.cancel");

    private static final int[] INCREMENTS_TOP = {1, 5, 10};
    private static final int[] INCREMENTS_BOTTOM = {-1, -5, -10};
    private static final int INCREMENT_BUTTON_WIDTH = 30;
    private static final int INCREMENT_BUTTON_X = 7;
    private static final int INCREMENT_BUTTON_TOP_Y = 20;
    private static final int INCREMENT_BUTTON_BOTTOM_Y = 64;

    private final Screen parent;
    private final PriorityAccessor priorityAccessor;

    private TextFieldWidget amountField;

    public PriorityScreen(PriorityAccessor priorityAccessor, Screen parent) {
        super(new DummyScreenHandler(), null, PriorityScreen.PRIORITY_TEXT);

        this.parent = parent;
        this.priorityAccessor = priorityAccessor;

        this.titleX = 7;
        this.titleY = 7;
        this.backgroundWidth = 164;
        this.backgroundHeight = 92;
    }

    @Override
    protected void init() {
        super.init();

        addButton(new ButtonWidget(x + ACTION_BUTTON_X, y + ACTION_BUTTON_Y, ACTION_BUTTON_WIDTH, 20, RESET_TEXT, btn -> reset()));
        addButton(new ButtonWidget(x + ACTION_BUTTON_X, y + ACTION_BUTTON_Y + 24, ACTION_BUTTON_WIDTH, 20, SET_TEXT, btn -> ok()));
        addButton(new ButtonWidget(x + ACTION_BUTTON_X, y + ACTION_BUTTON_Y + 48, ACTION_BUTTON_WIDTH, 20, CANCEL_TEXT, btn -> close()));

        amountField = new TextFieldWidget(textRenderer, x + AMOUNT_X, y + AMOUNT_Y, 69 - 6, textRenderer.fontHeight, new LiteralText(""));
        amountField.setDrawsBackground(false);
        amountField.setText(String.valueOf(priorityAccessor.getPriority()));
        amountField.setVisible(true);
        amountField.setFocusUnlocked(false);
        amountField.setTextFieldFocused(true);

        addButton(amountField);

        addIncrementButtons(INCREMENTS_TOP, x + INCREMENT_BUTTON_X, y + INCREMENT_BUTTON_TOP_Y);
        addIncrementButtons(INCREMENTS_BOTTOM, x + INCREMENT_BUTTON_X, y + INCREMENT_BUTTON_BOTTOM_Y);
    }

    private void addIncrementButtons(int[] increments, int x, int y) {
        for (int increment : increments) {
            Text text = new LiteralText((increment > 0 ? "+" : "") + increment);

            addButton(new ButtonWidget(x, y, INCREMENT_BUTTON_WIDTH, 20, text, btn -> changeAmount(increment)));

            x += INCREMENT_BUTTON_WIDTH + 3;
        }
    }

    private void changeAmount(int delta) {
        int oldAmount = getAmount();
        int newAmount = oldAmount + delta;
        amountField.setText(String.valueOf(newAmount));
    }

    private int getAmount() {
        try {
            return Integer.parseInt(amountField.getText());
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
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ScreenUtil.drawVersionInformation(matrices, textRenderer);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        textRenderer.draw(matrices, title, (float) titleX, (float) titleY, 4210752);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
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
        amountField.setText("0");
    }

    private void ok() {
        priorityAccessor.setPriority(getAmount());
        close();
    }

    private void close() {
        MinecraftClient.getInstance().openScreen(parent);
    }

    private static class DummyScreenHandler extends ScreenHandler {
        protected DummyScreenHandler() {
            super(null, 0);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }
}
