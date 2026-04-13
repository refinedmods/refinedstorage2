package com.refinedmods.refinedstorage.neoforge.datagen;

import com.refinedmods.refinedstorage.neoforge.datagen.loot.LootTableProviderImpl;
import com.refinedmods.refinedstorage.neoforge.datagen.model.ModelProviders;
import com.refinedmods.refinedstorage.neoforge.datagen.recipe.MainRecipeProvider;
import com.refinedmods.refinedstorage.neoforge.datagen.recipe.RecoloringRecipeProvider;
import com.refinedmods.refinedstorage.neoforge.datagen.tag.BlockTagsProvider;
import com.refinedmods.refinedstorage.neoforge.datagen.tag.ItemTagsProvider;

import java.util.List;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent.Client e) {
        final DataGenerator generator = e.getGenerator();
        final DataGenerator.PackGenerator pack = generator.getVanillaPack(true);
        pack.addProvider(ModelProviders::new);
        pack.addProvider(output -> new LootTableProviderImpl(output, e.getLookupProvider()));
        pack.addProvider(output -> new RecoloringRecipeProvider.Runner(output, e.getLookupProvider()));
        pack.addProvider(output -> new MainRecipeProvider.Runner(output, e.getLookupProvider()));
        final BlockTagsProvider blockTagsProvider = pack.addProvider(output ->
            new BlockTagsProvider(output, e.getLookupProvider()));
        pack.addProvider(output ->
            new ItemTagsProvider(output, e.getLookupProvider(), blockTagsProvider.contentsGetter()));
        pack.addProvider(output -> new AdvancementProvider(
            output,
            e.getLookupProvider(),
            List.of(new com.refinedmods.refinedstorage.neoforge.datagen.advancement.AdvancementProvider())
        ));
    }
}
