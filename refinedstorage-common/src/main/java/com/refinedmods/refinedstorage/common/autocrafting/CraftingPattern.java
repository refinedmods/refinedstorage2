package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CraftingPattern implements Pattern {
    private final UUID id;
    private final List<Ingredient> ingredients;
    private final ResourceAmount output;
    private final List<ResourceAmount> outputs;
    private final Set<ResourceKey> inputResources;
    private final Set<ResourceKey> outputResources;

    CraftingPattern(final UUID id,
                    final List<List<PlatformResourceKey>> inputs,
                    final ResourceAmount output,
                    final List<ResourceAmount> byproducts) {
        this.id = id;
        this.output = output;
        this.inputResources = inputs.stream().flatMap(List::stream).collect(Collectors.toSet());
        this.outputResources = Set.of(output.resource());
        this.ingredients = inputs.stream().map(i -> new Ingredient(i.isEmpty() ? 0 : 1, i)).toList();
        this.outputs = Stream.concat(Stream.of(output), byproducts.stream()).toList();
    }

    @Override
    public Set<ResourceKey> getOutputResources() {
        return outputResources;
    }

    @Override
    public Set<ResourceKey> getInputResources() {
        return inputResources;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public List<ResourceAmount> getOutputs() {
        return outputs;
    }

    ResourceAmount getOutput() {
        return output;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CraftingPattern that = (CraftingPattern) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
