package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.BaseScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseScreen.class)
public abstract class ResourceFilterSlotRenderingMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public ResourceFilterSlotRenderingMixin(T containerMenu, Inventory inventory, Component component) {
        super(containerMenu, inventory, component);
    }

    @Override
    public void renderSlot(PoseStack poseStack, Slot slot) {
        if (slot instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlot.render(poseStack, slot.x, slot.y, getBlitOffset());
        } else {
            super.renderSlot(poseStack, slot);
        }
    }
}
