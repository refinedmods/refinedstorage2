package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DiskInterfaceScreen extends AbstractBaseScreen<DiskInterfaceContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/disk_interface.png");

    private static final MutableComponent IN_TEXT = createTranslation("gui", "disk_interface.in");
    private static final MutableComponent OUT_TEXT = createTranslation("gui", "disk_interface.out");

    public DiskInterfaceScreen(final DiskInterfaceContainerMenu menu,
                               final Inventory playerInventory,
                               final Component title) {
        super(menu, playerInventory, title, 211, 211);
        this.inventoryLabelY = 117;
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
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int x, final int y) {
        super.extractLabels(graphics, x, y);
        graphics.text(font, IN_TEXT, 43, 45, -12566464, false);
        graphics.text(font, OUT_TEXT, 115, 45, -12566464, false);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
