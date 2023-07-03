package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.task.DefaultTaskExecutor;
import com.refinedmods.refinedstorage2.api.network.impl.node.task.RandomTaskExecutor;
import com.refinedmods.refinedstorage2.api.network.impl.node.task.RoundRobinTaskExecutor;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public enum SchedulingModeType {
    DEFAULT(0, createTranslation("gui", "scheduling_mode.default")),
    ROUND_ROBIN(1, createTranslation("gui", "scheduling_mode.round_robin")),
    RANDOM(2, createTranslation("gui", "scheduling_mode.random"));

    private static final String TAG_ROUND_ROBIN_INDEX = "rri";

    private final int id;
    private final MutableComponent name;

    SchedulingModeType(final int id, final MutableComponent name) {
        this.id = id;
        this.name = name;
    }

    public static SchedulingModeType getById(final int id) {
        for (final SchedulingModeType mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return DEFAULT;
    }

    public MutableComponent getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public <C> TaskExecutor<C> createTaskExecutor(@Nullable final CompoundTag tag,
                                                  final RandomTaskExecutor.Randomizer<C> randomizer,
                                                  final Runnable dirtyCallback) {
        return switch (this) {
            case DEFAULT -> new DefaultTaskExecutor<>();
            case RANDOM -> new RandomTaskExecutor<>(randomizer);
            case ROUND_ROBIN -> createRoundRobinExecutor(tag, dirtyCallback);
        };
    }

    private <C> RoundRobinTaskExecutor<C> createRoundRobinExecutor(@Nullable final CompoundTag tag,
                                                                   final Runnable dirtyCallback) {
        final int index = tag != null ? tag.getInt(TAG_ROUND_ROBIN_INDEX) : 0;
        return new RoundRobinTaskExecutor<>(new RoundRobinTaskExecutor.State(
            dirtyCallback,
            index
        ));
    }

    public <C> void writeToTag(final CompoundTag tag, final TaskExecutor<C> taskExecutor) {
        if (taskExecutor instanceof RoundRobinTaskExecutor<C> roundRobin) {
            tag.putInt(TAG_ROUND_ROBIN_INDEX, roundRobin.getIndex());
        }
    }
}
