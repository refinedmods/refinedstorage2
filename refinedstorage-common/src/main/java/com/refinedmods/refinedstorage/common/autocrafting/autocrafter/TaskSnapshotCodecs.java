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
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

final class TaskSnapshotCodecs {
    private static final String DESERIALIZE_ERROR_MESSAGE = """
        Refined Storage could not load an autocrafting task.
        This could be because a resource used in the task no longer exists after a mod update, or if the data format of
        the resource has changed. In any case, this is NOT caused by Refined Storage.
        Refined Storage will try to gracefully handle this problem and continue to load the other autocrafting tasks.
        The problematic autocrafting task will not be loaded.
        Error message:""";

    private static final Codec<ResourceList> RESOURCE_LIST_CODEC = Codec.list(ResourceCodecs.AMOUNT_CODEC).xmap(
        resources -> {
            final MutableResourceList result = MutableResourceListImpl.create();
            resources.forEach(result::add);
            return result;
        },
        list -> list.copyState().stream().toList()
    );

    private static final Codec<Ingredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("amount").forGetter(Ingredient::amount),
        Codec.list(ResourceCodecs.NATIVE_CODEC).fieldOf("inputs").forGetter(Ingredient::inputs)
    ).apply(instance, Ingredient::new));

    private static final Codec<Pattern> PATTERN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(Pattern::id),
        Codec.list(INGREDIENT_CODEC).fieldOf("ingredients").forGetter(p -> p.layout().ingredients()),
        Codec.list(ResourceCodecs.AMOUNT_CODEC).fieldOf("outputs").forGetter(p -> p.layout().outputs()),
        Codec.list(ResourceCodecs.AMOUNT_CODEC).fieldOf("byproducts").forGetter(p -> p.layout().byproducts()),
        Codec.BOOL.fieldOf("internal").forGetter(p -> p.layout().type() == PatternType.INTERNAL)
    ).apply(instance, (id, ingredients, outputs, byproducts, internal) -> new Pattern(
        id,
        Boolean.TRUE.equals(internal)
            ? PatternLayout.internal(ingredients, outputs, byproducts)
            : PatternLayout.external(ingredients, outputs)
    )));

    private static final Codec<TaskSnapshot.InternalPatternSnapshot> INTERNAL_PATTERN_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("originalIterationsRemaining")
                .forGetter(TaskSnapshot.InternalPatternSnapshot::originalIterationsRemaining),
            Codec.LONG.fieldOf("iterationsRemaining")
                .forGetter(TaskSnapshot.InternalPatternSnapshot::iterationsRemaining)
        ).apply(instance, TaskSnapshot.InternalPatternSnapshot::new));

    private static final Codec<TaskSnapshot.ExternalPatternSnapshot> EXTERNAL_PATTERN_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            RESOURCE_LIST_CODEC.fieldOf("expectedOutputs")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::expectedOutputs),
            RESOURCE_LIST_CODEC.fieldOf("simulatedIterationInputs")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::simulatedIterationInputs),
            Codec.LONG.fieldOf("originalIterationsToSendToSink")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::originalIterationsToSendToSink),
            Codec.LONG.fieldOf("iterationsToSendToSink")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::iterationsToSendToSink),
            Codec.LONG.fieldOf("iterationsReceived")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::iterationsReceived),
            Codec.BOOL.fieldOf("interceptedAnythingSinceLastStep")
                .forGetter(TaskSnapshot.ExternalPatternSnapshot::interceptedAnythingSinceLastStep),
            StringRepresentable.fromEnum(SerializableExternalPatternSinkResult::values)
                .optionalFieldOf("lastSinkResult").xmap(
                    result -> result.map(SerializableExternalPatternSinkResult::toResult),
                    result -> result.map(SerializableExternalPatternSinkResult::fromResult)
                ).forGetter(ts -> Optional.ofNullable(ts.lastSinkResult())),
            Codec.STRING.optionalFieldOf("lastSinkResultKeyName")
                .forGetter(snapshot -> snapshot.lastSinkResultKey() instanceof InWorldExternalPatternSinkKey key
                    ? Optional.of(key.name())
                    : Optional.empty()),
            ItemStack.CODEC.optionalFieldOf("lastSinkResultKeyStack").forGetter(
                snapshot -> snapshot.lastSinkResultKey() instanceof InWorldExternalPatternSinkKey key
                    ? Optional.of(key.stack())
                    : Optional.empty())
        ).apply(instance, (expectedOutputs, simulatedIterationInputs, originalIterationsToSendToSink,
                           iterationsToSendToSink, iterationsReceived, interceptedAnythingSinceLastStep,
                           lastSinkResult, lastSinkResultKeyName, lastSinkResultKeyStack) -> {
            final ExternalPatternSinkKey sinkKey =
                lastSinkResultKeyName.isPresent() && lastSinkResultKeyStack.isPresent()
                    ? new InWorldExternalPatternSinkKey(lastSinkResultKeyName.get(), lastSinkResultKeyStack.get())
                    : null;
            return new TaskSnapshot.ExternalPatternSnapshot(
                expectedOutputs,
                simulatedIterationInputs,
                originalIterationsToSendToSink,
                iterationsToSendToSink,
                iterationsReceived,
                interceptedAnythingSinceLastStep,
                lastSinkResult.orElse(null),
                sinkKey
            );
        }));

    private static final Codec<IngredientPossibilityEntry> INGREDIENT_POSSIBILITY_ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            ResourceCodecs.NATIVE_CODEC.fieldOf("resource").forGetter(IngredientPossibilityEntry::resource),
            Codec.LONG.fieldOf("available").forGetter(IngredientPossibilityEntry::available)
        ).apply(instance, IngredientPossibilityEntry::new));

    private static final Codec<IngredientEntry> INGREDIENT_ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(IngredientEntry::index),
            Codec.list(INGREDIENT_POSSIBILITY_ENTRY_CODEC).fieldOf("possibilities")
                .forGetter(IngredientEntry::possibilities)
        ).apply(instance, IngredientEntry::new));

    private static final Codec<Map<Integer, Map<ResourceKey, Long>>> INGREDIENTS_MAP_CODEC =
        Codec.list(INGREDIENT_ENTRY_CODEC).xmap(
            entries -> {
                final Map<Integer, Map<ResourceKey, Long>> result = new LinkedHashMap<>();
                for (final IngredientEntry entry : entries) {
                    final Map<ResourceKey, Long> possibilities = new LinkedHashMap<>();
                    for (final IngredientPossibilityEntry possibility : entry.possibilities()) {
                        possibilities.put(possibility.resource(), possibility.available());
                    }
                    result.put(entry.index(), possibilities);
                }
                return result;
            },
            map -> map.entrySet().stream().map(entry ->
                new IngredientEntry(
                    entry.getKey(),
                    entry.getValue().entrySet().stream().map(possibility ->
                        new IngredientPossibilityEntry(possibility.getKey(), possibility.getValue())
                    ).toList()
                )
            ).toList()
        );

    private static final Codec<TaskSnapshot.PatternSnapshot> PATTERN_SNAPSHOT_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("root").forGetter(TaskSnapshot.PatternSnapshot::root),
            PATTERN_CODEC.fieldOf("pattern").forGetter(TaskSnapshot.PatternSnapshot::pattern),
            INGREDIENTS_MAP_CODEC.fieldOf("ingredients").forGetter(TaskSnapshot.PatternSnapshot::ingredients),
            INTERNAL_PATTERN_CODEC.optionalFieldOf("internalPattern")
                .forGetter(snapshot -> Optional.ofNullable(snapshot.internalPattern())),
            EXTERNAL_PATTERN_CODEC.optionalFieldOf("externalPattern")
                .forGetter(ts -> Optional.ofNullable(ts.externalPattern()))
        ).apply(instance, (root, pattern, ingredients,
                           internalPattern, externalPattern) ->
            new TaskSnapshot.PatternSnapshot(
                root,
                pattern,
                ingredients,
                internalPattern.orElse(null),
                externalPattern.orElse(null)
            )));

    private static final Codec<PatternEntry> PATTERN_ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            PATTERN_CODEC.fieldOf("pattern").forGetter(PatternEntry::pattern),
            PATTERN_SNAPSHOT_CODEC.fieldOf("snapshot").forGetter(PatternEntry::snapshot)
        ).apply(instance, PatternEntry::new));

    // An unbounded map codec doesn't support non-string like keys, hence serialize like a list.
    private static final Codec<Map<Pattern, TaskSnapshot.PatternSnapshot>> PATTERN_MAP_CODEC =
        Codec.list(PATTERN_ENTRY_CODEC).xmap(
            entries -> {
                final Map<Pattern, TaskSnapshot.PatternSnapshot> map = new LinkedHashMap<>();
                for (final PatternEntry entry : entries) {
                    map.put(entry.pattern(), entry.snapshot());
                }
                return map;
            },
            map -> map.entrySet()
                .stream()
                .map(entry -> new PatternEntry(entry.getKey(), entry.getValue()))
                .toList()
        );

    private static final Codec<TaskSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("id").xmap(TaskId::new, TaskId::id).forGetter(TaskSnapshot::id),
        ResourceCodecs.NATIVE_CODEC.fieldOf("resource").forGetter(TaskSnapshot::resource),
        Codec.LONG.fieldOf("amount").forGetter(TaskSnapshot::amount),
        Codec.STRING.optionalFieldOf("actor").xmap(
            name -> name.map(n -> (Actor) new PlayerActor(n)).orElse(Actor.EMPTY),
            actor -> actor instanceof PlayerActor playerActor ? Optional.of(playerActor.getName()) : Optional.empty()
        ).forGetter(TaskSnapshot::actor),
        Codec.BOOL.fieldOf("notifyActor").forGetter(TaskSnapshot::notifyActor),
        Codec.LONG.fieldOf("startTime").forGetter(TaskSnapshot::startTime),
        PATTERN_MAP_CODEC.fieldOf("patterns").forGetter(TaskSnapshot::patterns),
        Codec.list(PATTERN_SNAPSHOT_CODEC).fieldOf("completedPatterns").forGetter(TaskSnapshot::completedPatterns),
        RESOURCE_LIST_CODEC.fieldOf("initialRequirements").forGetter(TaskSnapshot::initialRequirements),
        RESOURCE_LIST_CODEC.fieldOf("internalStorage").forGetter(TaskSnapshot::internalStorage),
        StringRepresentable.fromEnum(SerializableTaskState::values).fieldOf("state").xmap(
            SerializableTaskState::toTaskState,
            SerializableTaskState::fromTaskState
        ).forGetter(TaskSnapshot::state),
        Codec.BOOL.fieldOf("cancelled").forGetter(TaskSnapshot::cancelled)
    ).apply(instance, TaskSnapshot::new));

    public static final ErrorHandlingListCodec<TaskSnapshot> LIST_CODEC = new ErrorHandlingListCodec<>(
        CODEC,
        DESERIALIZE_ERROR_MESSAGE
    );

    private TaskSnapshotCodecs() {
    }

    private enum SerializableTaskState implements StringRepresentable {
        READY("ready"),
        EXTRACTING_INITIAL_RESOURCES("extracting_initial_resources"),
        RUNNING("running"),
        RETURNING_INTERNAL_STORAGE("returning_internal_storage"),
        COMPLETED("completed");

        private final String name;

        SerializableTaskState(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public TaskState toTaskState() {
            return switch (this) {
                case READY -> TaskState.READY;
                case EXTRACTING_INITIAL_RESOURCES -> TaskState.EXTRACTING_INITIAL_RESOURCES;
                case RUNNING -> TaskState.RUNNING;
                case RETURNING_INTERNAL_STORAGE -> TaskState.RETURNING_INTERNAL_STORAGE;
                case COMPLETED -> TaskState.COMPLETED;
            };
        }

        public static SerializableTaskState fromTaskState(final TaskState state) {
            return switch (state) {
                case READY -> READY;
                case EXTRACTING_INITIAL_RESOURCES -> EXTRACTING_INITIAL_RESOURCES;
                case RUNNING -> RUNNING;
                case RETURNING_INTERNAL_STORAGE -> RETURNING_INTERNAL_STORAGE;
                case COMPLETED -> COMPLETED;
            };
        }
    }

    private enum SerializableExternalPatternSinkResult implements StringRepresentable {
        ACCEPTED("accepted"),
        REJECTED("rejected"),
        SKIPPED("skipped"),
        LOCKED("locked");

        private final String name;

        SerializableExternalPatternSinkResult(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public ExternalPatternSink.Result toResult() {
            return switch (this) {
                case ACCEPTED -> ExternalPatternSink.Result.ACCEPTED;
                case REJECTED -> ExternalPatternSink.Result.REJECTED;
                case SKIPPED -> ExternalPatternSink.Result.SKIPPED;
                case LOCKED -> ExternalPatternSink.Result.LOCKED;
            };
        }

        public static SerializableExternalPatternSinkResult fromResult(final ExternalPatternSink.Result result) {
            return switch (result) {
                case ACCEPTED -> ACCEPTED;
                case REJECTED -> REJECTED;
                case SKIPPED -> SKIPPED;
                case LOCKED -> LOCKED;
            };
        }
    }

    private record PatternEntry(Pattern pattern, TaskSnapshot.PatternSnapshot snapshot) {
    }

    private record IngredientEntry(int index, List<IngredientPossibilityEntry> possibilities) {
    }

    private record IngredientPossibilityEntry(ResourceKey resource, long available) {
    }
}
