package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkDetails;

import net.minecraft.world.item.ItemStack;

public record AutocrafterExternalPatternSinkDetails(String name, ItemStack stack)
    implements ExternalPatternSinkDetails {
}
