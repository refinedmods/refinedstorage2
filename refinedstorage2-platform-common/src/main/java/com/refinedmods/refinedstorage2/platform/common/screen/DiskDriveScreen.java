package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.diskdrive.DiskDriveContainerMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DiskDriveScreen extends StorageScreen<DiskDriveContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/disk_drive.png");
    private static final TranslatableComponent DISKS_TEXT = createTranslation("gui", "disk_drive.disks");

    public DiskDriveScreen(DiskDriveContainerMenu menu, Inventory playerInventory, Component text) {
        super(menu, playerInventory, text, 99);
    }

    @Override
    protected boolean isResourceFilterButtonActive() {
        return true;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        super.renderBg(poseStack, delta, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        font.draw(poseStack, DISKS_TEXT, 60, 42, 4210752);
    }
}
