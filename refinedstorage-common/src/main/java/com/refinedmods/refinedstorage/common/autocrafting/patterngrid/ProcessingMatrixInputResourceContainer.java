package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToLongFunction;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

class ProcessingMatrixInputResourceContainer extends ResourceContainerImpl {
    private static final Codec<Set<Identifier>> IDENTIFIER_SET_CODEC = Identifier.CODEC.listOf().xmap(
        LinkedHashSet::new,
        ArrayList::new
    );
    public static final Codec<List<Set<Identifier>>> ALLOWED_TAG_IDS_CODEC = IDENTIFIER_SET_CODEC.listOf();

    private final List<Set<Identifier>> allowedTagIds;

    ProcessingMatrixInputResourceContainer(final int size,
                                           final ToLongFunction<ResourceKey> maxAmountProvider,
                                           final ResourceFactory primaryResourceFactory,
                                           final Set<ResourceFactory> alternativeResourceFactories) {
        super(size, maxAmountProvider, primaryResourceFactory, alternativeResourceFactories);
        this.allowedTagIds = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            allowedTagIds.add(Collections.emptySet());
        }
    }

    void set(final int index, final ProcessingPatternState.ProcessingIngredient processingIngredient) {
        setSilently(index, processingIngredient.input());
        allowedTagIds.set(index, new HashSet<>(processingIngredient.allowedAlternativeIds()));
        changed();
    }

    Optional<ProcessingPatternState.ProcessingIngredient> getIngredient(final int index) {
        return Optional.ofNullable(get(index)).map(input -> getIngredient(index, input));
    }

    private ProcessingPatternState.ProcessingIngredient getIngredient(final int index, final ResourceAmount input) {
        final Set<Identifier> allowedTagIdsOnIndex = allowedTagIds.get(index);
        final List<Identifier> ids = new ArrayList<>(allowedTagIdsOnIndex);
        return new ProcessingPatternState.ProcessingIngredient(input, ids);
    }

    List<Set<Identifier>> getAllowedTagIds() {
        return Collections.unmodifiableList(allowedTagIds);
    }

    Set<Identifier> getAllowedTagIds(final int index) {
        return Collections.unmodifiableSet(allowedTagIds.get(index));
    }

    void setAllowedTagIds(final int index, final Set<Identifier> ids) {
        if (index < 0 || index >= allowedTagIds.size()) {
            return;
        }
        allowedTagIds.set(index, ids);
        changed();
    }

    void setAllowedTagIdsSilently(final int index, final Set<Identifier> ids) {
        if (index < 0 || index >= allowedTagIds.size()) {
            return;
        }
        allowedTagIds.set(index, ids);
    }

    @Override
    protected void setSilently(final int index, final ResourceAmount resourceAmount) {
        super.setSilently(index, resourceAmount);
        allowedTagIds.set(index, Collections.emptySet());
    }

    @Override
    protected void removeSilently(final int index) {
        super.removeSilently(index);
        allowedTagIds.set(index, Collections.emptySet());
    }
}
