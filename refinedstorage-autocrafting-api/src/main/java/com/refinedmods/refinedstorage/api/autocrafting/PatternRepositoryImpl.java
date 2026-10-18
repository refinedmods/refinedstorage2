package com.refinedmods.refinedstorage.api.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class PatternRepositoryImpl implements PatternRepository {
    private final Set<Pattern> patterns = new HashSet<>();
    private final Set<Pattern> patternsView = Collections.unmodifiableSet(patterns);
    private final Map<ResourceKey, PriorityQueue<PatternHolder>> patternsByOutput = new HashMap<>();
    private final Set<ResourceKey> outputs = new HashSet<>();
    private final Set<ResourceKey> outputsView = Collections.unmodifiableSet(outputs);

    @Override
    public void add(final Pattern pattern, final int priority) {
        if (!patterns.add(pattern)) {
            update(pattern, priority);
            return;
        }
        pattern.layout().outputs().forEach(output -> outputs.add(output.resource()));
        for (final ResourceAmount output : pattern.layout().outputs()) {
            patternsByOutput.computeIfAbsent(output.resource(), k -> new PriorityQueue<>(
                Comparator.comparingInt(PatternHolder::priority).reversed()
            )).add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void update(final Pattern pattern, final int priority) {
        if (!patterns.contains(pattern)) {
            return;
        }
        for (final ResourceAmount output : pattern.layout().outputs()) {
            final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output.resource());
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            holders.add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void remove(final Pattern pattern) {
        patterns.remove(pattern);
        for (final ResourceAmount output : pattern.layout().outputs()) {
            final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output.resource());
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            if (holders.isEmpty()) {
                patternsByOutput.remove(output.resource());
            }
            final boolean noOtherPatternHasThisOutput = patterns.stream()
                .noneMatch(otherPattern -> otherPattern.layout().outputs().stream()
                    .anyMatch(o -> o.resource().equals(output.resource())));
            if (noOtherPatternHasThisOutput) {
                outputs.remove(output.resource());
            }
        }
    }

    @Override
    public List<Pattern> getByOutput(final ResourceKey output) {
        final PriorityQueue<PatternHolder> holders = patternsByOutput.get(output);
        if (holders == null) {
            return Collections.emptyList();
        }
        return holders.stream().map(holder -> holder.pattern).toList();
    }

    @Override
    public Set<ResourceKey> getOutputs() {
        return outputsView;
    }

    @Override
    public Set<Pattern> getAll() {
        return patternsView;
    }

    private record PatternHolder(Pattern pattern, int priority) {
    }
}
