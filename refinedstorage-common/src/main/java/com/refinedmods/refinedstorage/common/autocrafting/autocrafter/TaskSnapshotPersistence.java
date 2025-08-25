package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskSnapshot;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class TaskSnapshotPersistence {
    private static final String AMOUNT = "amount";
    private static final String INPUTS = "inputs";
    private static final String ORIGINAL_ITERATIONS_REMAINING = "originalIterationsRemaining";
    private static final String ITERATIONS_REMAINING = "iterationsRemaining";
    private static final String EXPECTED_OUTPUTS = "expectedOutputs";
    private static final String ITERATIONS_RECEIVED = "iterationsReceived";
    private static final String ITERATIONS_TO_SEND_TO_SINK = "iterationsToSendToSink";
    private static final String INTERCEPTED_ANYTHING_SINCE_LAST_STEP = "interceptedAnythingSinceLastStep";
    private static final String LAST_SINK_RESULT = "lastSinkResult";
    private static final String LAST_SINK_RESULT_KEY_NAME = "lastSinkResultKeyName";
    private static final String LAST_SINK_RESULT_KEY_STACK = "lastSinkResultKeyStack";
    private static final String PATTERN_MAP_KEY = "k";
    private static final String PATTERN_MAP_VALUE = "v";
    private static final String SIMULATED_ITERATION_INPUTS = "simulatedIterationInputs";
    private static final String ORIGINAL_ITERATIONS_TO_SEND_TO_SINK = "originalIterationsToSendToSink";
    private static final String PATTERN_TYPE = "type";
    private static final String INGREDIENTS = "ingredients";
    private static final String OUTPUTS = "outputs";
    private static final String BYPRODUCTS = "byproducts";
    private static final String ID = "id";
    private static final String EXTERNAL_PATTERN = "externalPattern";
    private static final String INTERNAL_PATTERN = "internalPattern";
    private static final String INTERNAL = "internal";
    private static final String ROOT = "root";
    private static final String PATTERN = "pattern";
    private static final String RESOURCE = "resource";
    private static final String ACTOR = "actor";
    private static final String NOTIFY_ACTOR = "notifyActor";
    private static final String START_TIME = "startTime";
    private static final String INITIAL_REQUIREMENTS = "initialRequirements";
    private static final String INTERNAL_STORAGE = "internalStorage";
    private static final String CANCELLED = "cancelled";
    private static final String TASK_STATE = "state";
    private static final String COMPLETED_PATTERNS = "completedPatterns";
    private static final String PATTERNS = "patterns";

    private TaskSnapshotPersistence() {
    }

    static CompoundTag encodeSnapshot(final TaskSnapshot snapshot) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID(ID, snapshot.id().id());
        final CompoundTag resourceTag = encodeResource(snapshot.resource());
        tag.put(RESOURCE, resourceTag);
        tag.putLong(AMOUNT, snapshot.amount());
        if (snapshot.actor() instanceof PlayerActor(String name)) {
            tag.putString(ACTOR, name);
        }
        tag.putBoolean(NOTIFY_ACTOR, snapshot.notifyActor());
        tag.putLong(START_TIME, snapshot.startTime());
        tag.put(INITIAL_REQUIREMENTS, encodeResourceList(snapshot.initialRequirements()));
        tag.put(INTERNAL_STORAGE, encodeResourceList(snapshot.internalStorage()));
        tag.putBoolean(CANCELLED, snapshot.cancelled());
        tag.putString(TASK_STATE, snapshot.state().name());
        final ListTag completedPatterns = new ListTag();
        snapshot.completedPatterns().forEach(pattern -> completedPatterns.add(encodePatternSnapshot(pattern)));
        tag.put(COMPLETED_PATTERNS, completedPatterns);
        tag.put(PATTERNS, encodePatternMap(snapshot.patterns()));
        return tag;
    }

    private static ListTag encodeResourceList(final ResourceList list) {
        final ListTag listTag = new ListTag();
        list.getAll().forEach(resource -> {
            final CompoundTag entryTag = encodeResource(resource);
            entryTag.putLong(AMOUNT, list.get(resource));
            listTag.add(entryTag);
        });
        return listTag;
    }

    private static CompoundTag encodePattern(final Pattern pattern) {
        final CompoundTag tag = new CompoundTag();
        tag.putUUID(ID, pattern.id());
        final ListTag ingredients = new ListTag();
        for (final Ingredient ingredient : pattern.layout().ingredients()) {
            ingredients.add(encodeIngredient(ingredient));
        }
        tag.put(INGREDIENTS, ingredients);
        final ListTag outputs = new ListTag();
        for (final ResourceAmount output : pattern.layout().outputs()) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.encode(output, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        }
        tag.put(OUTPUTS, outputs);
        final ListTag byproducts = new ListTag();
        for (final ResourceAmount byproduct : pattern.layout().byproducts()) {
            byproducts.add(ResourceCodecs.AMOUNT_CODEC.encode(byproduct, NbtOps.INSTANCE,
                new CompoundTag()).getOrThrow());
        }
        tag.put(BYPRODUCTS, byproducts);
        tag.putString(PATTERN_TYPE, pattern.layout().type().name());
        return tag;
    }

    private static CompoundTag encodePatternSnapshot(final TaskSnapshot.PatternSnapshot snapshot) {
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean(ROOT, snapshot.root());
        tag.put(PATTERN, encodePattern(snapshot.pattern()));
        tag.put(INGREDIENTS, encodeIngredientMap(snapshot.ingredients()));
        final boolean internal = snapshot.internalPattern() != null;
        tag.putBoolean(INTERNAL, internal);
        if (snapshot.internalPattern() != null) {
            tag.put(INTERNAL_PATTERN, encodeInternalPattern(snapshot.internalPattern()));
        } else if (snapshot.externalPattern() != null) {
            tag.put(EXTERNAL_PATTERN, encodeExternalPattern(snapshot.externalPattern()));
        }
        return tag;
    }

    private static ListTag encodePatternMap(final Map<Pattern, TaskSnapshot.PatternSnapshot> patterns) {
        final ListTag patternMap = new ListTag();
        for (final var pattern : patterns.entrySet()) {
            final CompoundTag patternTag = new CompoundTag();
            patternTag.put(PATTERN_MAP_KEY, encodePattern(pattern.getKey()));
            patternTag.put(PATTERN_MAP_VALUE, encodePatternSnapshot(pattern.getValue()));
            patternMap.add(patternTag);
        }
        return patternMap;
    }

    private static CompoundTag encodeInternalPattern(final TaskSnapshot.InternalPatternSnapshot internalPattern) {
        final CompoundTag tag = new CompoundTag();
        tag.putLong(ORIGINAL_ITERATIONS_REMAINING, internalPattern.originalIterationsRemaining());
        tag.putLong(ITERATIONS_REMAINING, internalPattern.iterationsRemaining());
        return tag;
    }

    private static CompoundTag encodeExternalPattern(final TaskSnapshot.ExternalPatternSnapshot externalPattern) {
        final CompoundTag tag = new CompoundTag();
        tag.put(EXPECTED_OUTPUTS, encodeResourceList(externalPattern.expectedOutputs()));
        tag.put(SIMULATED_ITERATION_INPUTS, encodeResourceList(externalPattern.simulatedIterationInputs()));
        tag.putLong(ORIGINAL_ITERATIONS_TO_SEND_TO_SINK, externalPattern.originalIterationsToSendToSink());
        tag.putLong(ITERATIONS_TO_SEND_TO_SINK, externalPattern.iterationsToSendToSink());
        tag.putLong(ITERATIONS_RECEIVED, externalPattern.iterationsReceived());
        tag.putBoolean(INTERCEPTED_ANYTHING_SINCE_LAST_STEP, externalPattern.interceptedAnythingSinceLastStep());
        if (externalPattern.lastSinkResult() != null) {
            tag.putString(LAST_SINK_RESULT, externalPattern.lastSinkResult().name());
        }
        final ExternalPatternSinkKey lastSinkResultKey = externalPattern.lastSinkResultKey();
        if (lastSinkResultKey instanceof InWorldExternalPatternSinkKey(String name, ItemStack stack)) {
            tag.putString(LAST_SINK_RESULT_KEY_NAME, name);
            tag.put(LAST_SINK_RESULT_KEY_STACK, ItemStack.CODEC
                .encode(stack, NbtOps.INSTANCE, new CompoundTag()).getOrThrow());
        }
        return tag;
    }

    private static ListTag encodeIngredientMap(final Map<Integer, Map<ResourceKey, Long>> ingredients) {
        final ListTag ingredientMap = new ListTag();
        for (final var ingredient : ingredients.entrySet()) {
            final CompoundTag ingredientTag = new CompoundTag();
            ingredientTag.putInt("i", ingredient.getKey());
            ingredientTag.put(PATTERN_MAP_VALUE, encodeIngredientResources(ingredient));
            ingredientMap.add(ingredientTag);
        }
        return ingredientMap;
    }

    private static ListTag encodeIngredientResources(final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient) {
        final ListTag ingredientResources = new ListTag();
        for (final var resourceAndAmount : ingredient.getValue().entrySet()) {
            final CompoundTag tag = encodeResource(resourceAndAmount.getKey());
            tag.putLong(AMOUNT, resourceAndAmount.getValue());
            ingredientResources.add(tag);
        }
        return ingredientResources;
    }

    private static CompoundTag encodeResource(final ResourceKey resource) {
        return (CompoundTag) ResourceCodecs.CODEC.encode((PlatformResourceKey) resource, NbtOps.INSTANCE,
            new CompoundTag()).getOrThrow();
    }

    private static CompoundTag encodeIngredient(final Ingredient ingredient) {
        final CompoundTag ingredientTag = new CompoundTag();
        ingredientTag.putLong(AMOUNT, ingredient.amount());
        final ListTag inputsTag = new ListTag();
        for (final ResourceKey input : ingredient.inputs()) {
            inputsTag.add(encodeResource(input));
        }
        ingredientTag.put(INPUTS, inputsTag);
        return ingredientTag;
    }

    static TaskSnapshot decodeSnapshot(final CompoundTag tag) {
        final UUID id = tag.getUUID(ID);
        final ResourceKey resource = decodeResource(tag.getCompound(RESOURCE));
        final long amount = tag.getLong(AMOUNT);
        final Actor actor = tag.contains(ACTOR, Tag.TAG_STRING)
            ? new PlayerActor(tag.getString(ACTOR))
            : Actor.EMPTY;
        final boolean notifyActor = tag.getBoolean(NOTIFY_ACTOR);
        final long startTime = tag.getLong(START_TIME);
        final ResourceList initialRequirements = decodeResourceList(
            tag.getList(INITIAL_REQUIREMENTS, Tag.TAG_COMPOUND)
        );
        final ResourceList internalStorage = decodeResourceList(
            tag.getList(INTERNAL_STORAGE, Tag.TAG_COMPOUND)
        );
        final boolean cancelled = tag.getBoolean(CANCELLED);
        final TaskState state = TaskState.valueOf(tag.getString(TASK_STATE));
        final List<TaskSnapshot.PatternSnapshot> completedPatterns = new ArrayList<>();
        for (final Tag completedTag : tag.getList(COMPLETED_PATTERNS, Tag.TAG_COMPOUND)) {
            completedPatterns.add(decodePatternSnapshot((CompoundTag) completedTag));
        }
        final var patterns = decodePatternMap(tag.getList(PATTERNS, Tag.TAG_COMPOUND));
        return new TaskSnapshot(
            new TaskId(id),
            resource,
            amount,
            actor,
            notifyActor,
            startTime,
            patterns,
            completedPatterns,
            initialRequirements,
            internalStorage,
            state,
            cancelled
        );
    }

    private static ResourceList decodeResourceList(final ListTag listTag) {
        final MutableResourceList resourceList = MutableResourceListImpl.create();
        for (final Tag tag : listTag) {
            final CompoundTag entryTag = (CompoundTag) tag;
            final ResourceKey resource = decodeResource(entryTag);
            final long amount = entryTag.getLong(AMOUNT);
            resourceList.add(resource, amount);
        }
        return resourceList;
    }

    private static ResourceKey decodeResource(final CompoundTag resourceTag) {
        return ResourceCodecs.CODEC.parse(NbtOps.INSTANCE, resourceTag).result().orElseThrow();
    }

    private static TaskSnapshot.PatternSnapshot decodePatternSnapshot(final CompoundTag tag) {
        final boolean root = tag.getBoolean(ROOT);
        final Pattern pattern = decodePattern(tag.getCompound(PATTERN));
        final var ingredients = decodeIngredientMap(tag.getList(INGREDIENTS, Tag.TAG_COMPOUND));
        if (tag.getBoolean(INTERNAL)) {
            final TaskSnapshot.InternalPatternSnapshot internalPattern = decodeInternalPattern(
                tag.getCompound(INTERNAL_PATTERN)
            );
            return new TaskSnapshot.PatternSnapshot(root, pattern, ingredients, internalPattern, null);
        }
        final TaskSnapshot.ExternalPatternSnapshot externalPattern = decodeExternalPattern(
            tag.getCompound(EXTERNAL_PATTERN)
        );
        return new TaskSnapshot.PatternSnapshot(root, pattern, ingredients, null, externalPattern);
    }

    private static Pattern decodePattern(final CompoundTag tag) {
        final UUID id = tag.getUUID(ID);
        final List<Ingredient> ingredients = new ArrayList<>();
        for (final Tag ingredientTag : tag.getList(INGREDIENTS, Tag.TAG_COMPOUND)) {
            ingredients.add(decodeIngredient((CompoundTag) ingredientTag));
        }
        final List<ResourceAmount> outputs = new ArrayList<>();
        for (final Tag outputTag : tag.getList(OUTPUTS, Tag.TAG_COMPOUND)) {
            outputs.add(ResourceCodecs.AMOUNT_CODEC.parse(NbtOps.INSTANCE, outputTag).result().orElseThrow());
        }
        final List<ResourceAmount> byproducts = new ArrayList<>();
        for (final Tag byproductTag : tag.getList(BYPRODUCTS, Tag.TAG_COMPOUND)) {
            byproducts.add(ResourceCodecs.AMOUNT_CODEC.parse(NbtOps.INSTANCE, byproductTag).result().orElseThrow());
        }
        final PatternType type = PatternType.valueOf(tag.getString(PATTERN_TYPE));
        return new Pattern(id, new PatternLayout(ingredients, outputs, byproducts, type));
    }

    private static Ingredient decodeIngredient(final CompoundTag tag) {
        final long amount = tag.getLong(AMOUNT);
        final List<ResourceKey> inputs = new ArrayList<>();
        for (final Tag inputTag : tag.getList(INPUTS, Tag.TAG_COMPOUND)) {
            inputs.add(decodeResource((CompoundTag) inputTag));
        }
        return new Ingredient(amount, inputs);
    }

    private static TaskSnapshot.InternalPatternSnapshot decodeInternalPattern(final CompoundTag tag) {
        final long originalIterationsRemaining = tag.getLong(ORIGINAL_ITERATIONS_REMAINING);
        final long iterationsRemaining = tag.getLong(ITERATIONS_REMAINING);
        return new TaskSnapshot.InternalPatternSnapshot(originalIterationsRemaining, iterationsRemaining);
    }

    private static TaskSnapshot.ExternalPatternSnapshot decodeExternalPattern(final CompoundTag tag) {
        final ResourceList expectedOutputs = decodeResourceList(tag.getList(EXPECTED_OUTPUTS, Tag.TAG_COMPOUND));
        final ResourceList simulatedIterationInputs =
            decodeResourceList(tag.getList(SIMULATED_ITERATION_INPUTS, Tag.TAG_COMPOUND));
        final long originalIterationsToSendToSink = tag.getLong(ORIGINAL_ITERATIONS_TO_SEND_TO_SINK);
        final long iterationsToSendToSink = tag.getLong(ITERATIONS_TO_SEND_TO_SINK);
        final long iterationsReceived = tag.getLong(ITERATIONS_RECEIVED);
        final boolean interceptedAnythingSinceLastStep = tag.getBoolean(INTERCEPTED_ANYTHING_SINCE_LAST_STEP);
        final ExternalPatternSink.Result lastSinkResult = tag.contains(LAST_SINK_RESULT, Tag.TAG_STRING)
            ? ExternalPatternSink.Result.valueOf(tag.getString(LAST_SINK_RESULT))
            : null;
        final ExternalPatternSinkKey lastSinkResultKey = tag.contains(LAST_SINK_RESULT_KEY_NAME, Tag.TAG_STRING)
            ? decodeSinkResultKey(tag)
            : null;
        return new TaskSnapshot.ExternalPatternSnapshot(
            expectedOutputs,
            simulatedIterationInputs,
            originalIterationsToSendToSink,
            iterationsToSendToSink,
            iterationsReceived,
            interceptedAnythingSinceLastStep,
            lastSinkResult,
            lastSinkResultKey
        );
    }

    private static InWorldExternalPatternSinkKey decodeSinkResultKey(final CompoundTag tag) {
        return new InWorldExternalPatternSinkKey(
            tag.getString(LAST_SINK_RESULT_KEY_NAME),
            ItemStack.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(LAST_SINK_RESULT_KEY_STACK)).result().orElseThrow()
        );
    }

    private static Map<Pattern, TaskSnapshot.PatternSnapshot> decodePatternMap(final ListTag patternMapTag) {
        final Map<Pattern, TaskSnapshot.PatternSnapshot> patternMap = new LinkedHashMap<>();
        for (final Tag tag : patternMapTag) {
            final CompoundTag entry = (CompoundTag) tag;
            final Pattern key = decodePattern(entry.getCompound(PATTERN_MAP_KEY));
            final TaskSnapshot.PatternSnapshot value = decodePatternSnapshot(entry.getCompound(PATTERN_MAP_VALUE));
            patternMap.put(key, value);
        }
        return patternMap;
    }

    private static Map<Integer, Map<ResourceKey, Long>> decodeIngredientMap(final ListTag ingredientMapTag) {
        final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();
        for (final Tag tag : ingredientMapTag) {
            final CompoundTag entry = (CompoundTag) tag;
            final int index = entry.getInt("i");
            final Map<ResourceKey, Long> resources = decodeIngredientResources(
                entry.getList(PATTERN_MAP_VALUE, Tag.TAG_COMPOUND)
            );
            ingredients.put(index, resources);
        }
        return ingredients;
    }

    private static Map<ResourceKey, Long> decodeIngredientResources(final ListTag ingredientResources) {
        final Map<ResourceKey, Long> resources = new LinkedHashMap<>();
        for (final Tag rawTag : ingredientResources) {
            final CompoundTag tag = (CompoundTag) rawTag;
            final ResourceKey resource = decodeResource(tag);
            final long amount = tag.getLong(AMOUNT);
            resources.put(resource, amount);
        }
        return resources;
    }
}
