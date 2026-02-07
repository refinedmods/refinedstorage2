package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.AbstractProgressStorageScreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DiskDriveScreen extends AbstractProgressStorageScreen<DiskDriveContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/disk_drive.png");
    private static final MutableComponent DISKS_TEXT = createTranslation("gui", "disk_drive.disks");

    public DiskDriveScreen(final DiskDriveContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title, 99);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, DISKS_TEXT, 60, 42, -12566464, false);
    }
}
