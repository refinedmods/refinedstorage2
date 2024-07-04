package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.platform.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class DiskInterfaceScreen extends AbstractBaseScreen<DiskInterfaceContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/disk_interface.png");

    private static final MutableComponent IN_TEXT = createTranslation("gui", "disk_interface.in");
    private static final MutableComponent OUT_TEXT = createTranslation("gui", "disk_interface.out");

    public DiskInterfaceScreen(final DiskInterfaceContainerMenu menu,
                               final Inventory playerInventory,
                               final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 117;
        this.imageWidth = 211;
        this.imageHeight = 211;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new TransferModeSideButtonWidget(
            getMenu().getProperty(DiskInterfacePropertyTypes.TRANSFER_MODE)
        ));
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            createTranslation("gui", "disk_interface.filter_mode.allow.help"),
            createTranslation("gui", "disk_interface.filter_mode.block.help")
        ));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> getMenu().getProperty(DiskInterfacePropertyTypes.TRANSFER_MODE).getValue()
                == StorageTransferMode.EXTRACT_FROM_NETWORK
                ? FuzzyModeSideButtonWidget.Type.EXTRACTING_STORAGE_NETWORK
                : FuzzyModeSideButtonWidget.Type.EXTRACTING_SOURCE
        ));
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int x, final int y) {
        super.renderLabels(graphics, x, y);
        graphics.drawString(font, IN_TEXT, 43, 45, 4210752, false);
        graphics.drawString(font, OUT_TEXT, 115, 45, 4210752, false);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
