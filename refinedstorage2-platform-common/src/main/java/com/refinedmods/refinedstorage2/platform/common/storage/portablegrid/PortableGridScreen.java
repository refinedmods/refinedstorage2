package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage2.platform.common.support.widget.ProgressWidget;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class PortableGridScreen extends AbstractGridScreen<AbstractPortableGridContainerMenu> {
    private static final int DISK_SLOT_WIDTH = 30;
    private static final int DISK_SLOT_HEIGHT = 26;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/portable_grid.png");

    @Nullable
    private ProgressWidget progressWidget;

    public PortableGridScreen(final AbstractPortableGridContainerMenu menu,
                              final Inventory inventory,
                              final Component title) {
        super(menu, inventory, title, 99);
        this.inventoryLabelY = 75;
        this.imageWidth = 193;
        this.imageHeight = 176;
    }

    @Override
    protected int getSideButtonY() {
        return super.getSideButtonY() + DISK_SLOT_HEIGHT;
    }

    @Override
    protected boolean hasClickedOutside(final double mouseX,
                                        final double mouseY,
                                        final int leftPos,
                                        final int topPos,
                                        final int clickedButton) {
        if (mouseX >= leftPos - DISK_SLOT_WIDTH + 3 && mouseX <= leftPos
            && mouseY >= topPos + 3 && mouseY <= topPos + 3 + DISK_SLOT_HEIGHT) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, leftPos, topPos, clickedButton);
    }

    @Override
    protected void init() {
        super.init();
        getExclusionZones().add(new Rect2i(
            leftPos - DISK_SLOT_WIDTH + 3,
            topPos + 3,
            DISK_SLOT_WIDTH,
            DISK_SLOT_HEIGHT
        ));
        final int progressX = 172;
        final int progressY = imageHeight - 10 - 70;
        if (progressWidget == null) {
            progressWidget = new ProgressWidget(
                leftPos + progressX,
                topPos + progressY,
                16,
                70,
                getMenu().getEnergyInfo()::getPercentageFull,
                getMenu().getEnergyInfo()::createTooltip
            );
        } else {
            progressWidget.setX(leftPos + progressX);
            progressWidget.setY(topPos + progressY);
        }
        addRenderableWidget(progressWidget);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        graphics.blit(getTexture(), x - DISK_SLOT_WIDTH + 3, y + 3, 226, 0, DISK_SLOT_WIDTH, DISK_SLOT_HEIGHT);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
