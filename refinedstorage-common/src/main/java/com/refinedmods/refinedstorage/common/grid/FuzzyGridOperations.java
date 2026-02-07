package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.net.URI;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FuzzyGridOperations implements GridOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuzzyGridOperations.class);

    private static final Component INACCURATE_EXTRACTION = createTranslation("gui", "grid.inaccurate_extraction");
    private static final Component CHECK_CHAT_FOR_MORE_DETAILS =
        createTranslation("gui", "grid.inaccurate_extraction.check_chat_for_more_details");
    private static final Component READ_MORE = createTranslation("gui", "grid.inaccurate_extraction.read_more")
        .setStyle(Style.EMPTY
            .withClickEvent(new ClickEvent.OpenUrl(
                URI.create("https://refinedmods.com/refined-storage/troubleshooting/inaccurate-extraction.html")))
            .withColor(ChatFormatting.BLUE)
            .withUnderlined(true));

    private final ServerPlayer player;
    private final FuzzyRootStorage fuzzyRootStorage;
    private final GridOperations delegate;

    public FuzzyGridOperations(final ServerPlayer player,
                               final FuzzyRootStorage fuzzyRootStorage,
                               final GridOperations delegate) {
        this.player = player;
        this.fuzzyRootStorage = fuzzyRootStorage;
        this.delegate = delegate;
    }

    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        final boolean success = delegate.extract(resource, extractMode, destination);
        if (!success) {
            LOGGER.warn("Failed to extract resource in Grid: {}", resource);
            if (resource instanceof ItemResource itemResource) {
                itemResource.components().entrySet().forEach(e -> {
                    final DataComponentType<?> componentType = e.getKey();
                    final Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
                    LOGGER.warn("Component {} = {}", key, e.getValue());
                });
            }
        }
        if (!success && tryFuzzyExtractBecauseModHasUnstableDataComponentEquality(resource, extractMode, destination)) {
            return true;
        }
        return success;
    }

    private boolean tryFuzzyExtractBecauseModHasUnstableDataComponentEquality(final ResourceKey resource,
                                                                              final GridExtractMode extractMode,
                                                                              final InsertableStorage destination) {
        for (final ResourceKey fuzzyResource : fuzzyRootStorage.getFuzzy(resource)) {
            if (delegate.extract(fuzzyResource, extractMode, destination)) {
                final String modId = getModId(resource);
                final Component baseMessage = createTranslation(
                    "gui",
                    "grid.inaccurate_extraction.due_to_bug_in_other_mod_wrong_resource_maybe_extracted",
                    modId
                ).append(" ");
                RefinedStorageApi.INSTANCE.sendMessage(player, INACCURATE_EXTRACTION,
                    baseMessage.copy().append(CHECK_CHAT_FOR_MORE_DETAILS));
                player.sendSystemMessage(baseMessage.copy().append(READ_MORE));
                return true;
            }
        }
        return false;
    }

    private static String getModId(final ResourceKey resource) {
        if (resource instanceof ItemResource itemResource) {
            return BuiltInRegistries.ITEM.getKey(itemResource.item()).getNamespace();
        } else if (resource instanceof FluidResource fluidResource) {
            return BuiltInRegistries.FLUID.getKey(fluidResource.fluid()).getNamespace();
        }
        return "<unknown>";
    }

    @Override
    public boolean insert(final ResourceKey resource, final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        return delegate.insert(resource, insertMode, source);
    }
}
