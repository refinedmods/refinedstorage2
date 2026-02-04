package com.refinedmods.refinedstorage.common.cape;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class TenthAnniversaryScreen extends AbstractBaseScreen<TenthAnniversaryScreen.DefaultDummyContainerMenu> {
    private static final String URL = "https://refinedmods.com/refined-storage/news/20260320-ten-years-of-refined-storage.html";
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/tenth_anniversary.png");

    private static final Component TITLE = createTranslation("gui", "tenth_anniversary.title");
    private static final Component ENABLE_CAPE = createTranslation("gui", "tenth_anniversary.enable_cape");
    private static final Component DISABLE_CAPE = createTranslation("gui", "tenth_anniversary.disable_cape");
    private static final Component CAPE_INFO = createTranslation("gui", "tenth_anniversary.cape_info");
    private static final Component READ_MORE = createTranslation("gui", "tenth_anniversary.read_more");
    private static final Component BACK = createTranslation("gui", "tenth_anniversary.back");

    private final Screen parent;

    public TenthAnniversaryScreen(final Inventory playerInventory, final Screen parent) {
        super(new DefaultDummyContainerMenu(), playerInventory, Component.empty());
        this.imageWidth = 173;
        this.imageHeight = 93;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        final boolean capeEnabled = Platform.INSTANCE.getConfig().isTenthAnniversaryCape();
        addRenderableWidget(Button.builder(capeEnabled ? DISABLE_CAPE : ENABLE_CAPE, button -> {
            final boolean enabledNow = !Platform.INSTANCE.getConfig().isTenthAnniversaryCape();
            Platform.INSTANCE.getConfig().setTenthAnniversaryCape(enabledNow);
            button.setMessage(enabledNow ? DISABLE_CAPE : ENABLE_CAPE);
        }).tooltip(Tooltip.create(CAPE_INFO)).bounds(leftPos + 5, topPos + 20, 162, 20).build());
        addRenderableWidget(Button.builder(READ_MORE, button -> Util.getPlatform().openUri(URL))
            .bounds(leftPos + 5, topPos + 20 + 20 + 3, 162, 20).build());
        addRenderableWidget(Button.builder(BACK, button -> close())
            .bounds(leftPos + 5, topPos + 20 + 20 + 3 + 20 + 3, 162, 20).build());
    }

    private boolean tryClose(final int key) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    private void close() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (tryClose(key)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawCenteredString(font, TITLE, imageWidth / 2, 7, 16777045);
    }

    protected static class DefaultDummyContainerMenu extends AbstractContainerMenu {
        protected DefaultDummyContainerMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
