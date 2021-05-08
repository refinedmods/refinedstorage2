package com.refinedmods.refinedstorage2.fabric.mixin;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelPredicateProviderRegistry.class)
public interface ModelPredicateProviderRegistryAccessor {
    @Invoker("register")
    public static void register(Item item, Identifier id, ModelPredicateProvider provider) {
        throw new AssertionError();
    }
}
