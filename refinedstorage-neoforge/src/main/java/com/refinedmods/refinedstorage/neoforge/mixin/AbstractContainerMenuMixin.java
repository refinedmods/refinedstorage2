package com.refinedmods.refinedstorage.neoforge.mixin;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItemHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerMenuMixin.class);

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    public abstract void setCarried(ItemStack stack);

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    public void doClick(final int slotId,
                        final int button,
                        final ContainerInput input,
                        final Player player,
                        final CallbackInfo ci) {
        if (input != ContainerInput.CLONE
            || !player.hasInfiniteMaterials()
            || !this.getCarried().isEmpty()
            || slotId < 0) {
            return;
        }

        final Slot slot = this.slots.get(slotId);
        if (!slot.hasItem()) {
            return;
        }

        final ItemStack stack = slot.getItem();
        final StorageContainerItemHelper helper = RefinedStorageApi.INSTANCE.getStorageContainerItemHelper();

        final ItemStack copy = stack.copy();
        if (helper.clear(copy)) {
            LOGGER.debug("Cleared storage reference of storage container {}", copy);
            this.setCarried(copy.copyWithCount(stack.getMaxStackSize()));
            ci.cancel();
        }
    }
}
