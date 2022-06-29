package com.refinedmods.refinedstorage2.platform.fabric.mixin;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemProperties.class)
public interface ItemPropertiesAccessor {
    @Invoker("register")
    static void register(Item item, ResourceLocation id, ClampedItemPropertyFunction property) {
        throw new AssertionError();
    }
}
