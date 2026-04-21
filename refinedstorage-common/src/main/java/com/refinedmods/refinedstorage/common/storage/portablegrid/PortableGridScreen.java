package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.widget.ProgressWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class PortableGridScreen extends AbstractGridScreen<AbstractPortableGridContainerMenu> {
    private static final int DISK_SLOT_WIDTH = 30;
    private static final int DISK_SLOT_HEIGHT = 26;

    private static final Identifier ENERGY_AND_DISK_SLOT_TEXTURE = createIdentifier("textures/gui/portable_grid.png");
    private static final Identifier NO_ENERGY_TEXTURE = createIdentifier("textures/gui/grid.png");

    @Nullable
    private ProgressWidget progressWidget;
    private final Identifier texture;

    public PortableGridScreen(final AbstractPortableGridContainerMenu menu,
                              final Inventory inventory,
                              final Component title) {
        super(menu, inventory, title, 99, 193, 176);
        this.inventoryLabelY = 75;
        this.texture = RefinedStorageApi.INSTANCE.isEnergyRequired()
            ? ENERGY_AND_DISK_SLOT_TEXTURE
            : NO_ENERGY_TEXTURE;
    }

    @Override
    protected int getSideButtonY() {
        return super.getSideButtonY() + DISK_SLOT_HEIGHT;
    }

    @Override
    protected boolean hasClickedOutside(final double mouseX,
                                        final double mouseY,
                                        final int leftPos,
                                        final int topPos) {
        if (mouseX >= leftPos - DISK_SLOT_WIDTH + 3 && mouseX <= leftPos
            && mouseY >= topPos + 3 && mouseY <= topPos + 3 + DISK_SLOT_HEIGHT) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, leftPos, topPos);
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
        if (!RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            return;
        }
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
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURED, ENERGY_AND_DISK_SLOT_TEXTURE, x - DISK_SLOT_WIDTH + 3, y + 3, 226, 0,
            DISK_SLOT_WIDTH, DISK_SLOT_HEIGHT, 256, 256);
    }

    @Override
    protected Identifier getTexture() {
        return texture;
    }
}
