package com.refinedmods.refinedstorage.common.api.autocrafting;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.beta.9")
public interface PatternOutputRenderingScreen {
    boolean canDisplayOutput(ItemStack stack);
}
