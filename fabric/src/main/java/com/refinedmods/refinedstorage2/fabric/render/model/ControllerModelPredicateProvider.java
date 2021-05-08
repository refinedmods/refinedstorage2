package com.refinedmods.refinedstorage2.fabric.render.model;

import com.refinedmods.refinedstorage2.fabric.item.block.ControllerBlockItem;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ControllerModelPredicateProvider implements ModelPredicateProvider {
    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        return ControllerBlockItem.getPercentFull(stack);
    }
}
