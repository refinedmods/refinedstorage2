package com.refinedmods.refinedstorage2.platform.common.block.entity.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling.FirstAvailableExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling.RandomExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling.RoundRobinExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling.RoundRobinState;
import com.refinedmods.refinedstorage2.api.network.node.exporter.scheduling.ExporterSchedulingMode;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public enum ExporterSchedulingModeSettings {
    FIRST_AVAILABLE(0, createTranslation("gui", "exporter.scheduling_mode.first_available")),
    ROUND_ROBIN(1, createTranslation("gui", "exporter.scheduling_mode.round_robin")),
    RANDOM(2, createTranslation("gui", "exporter.scheduling_mode.random"));

    private static final String TAG_ROUND_ROBIN_INDEX = "rri";

    private final int id;
    private final MutableComponent name;

    ExporterSchedulingModeSettings(final int id, final MutableComponent name) {
        this.id = id;
        this.name = name;
    }

    public static ExporterSchedulingModeSettings getById(final int id) {
        for (final ExporterSchedulingModeSettings mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return FIRST_AVAILABLE;
    }

    public MutableComponent getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public ExporterSchedulingMode create(@Nullable final CompoundTag tag,
                                         final RandomExporterSchedulingMode.Randomizer randomizer,
                                         final Runnable dirtyCallback) {
        return switch (this) {
            case FIRST_AVAILABLE -> FirstAvailableExporterSchedulingMode.INSTANCE;
            case RANDOM -> new RandomExporterSchedulingMode(randomizer);
            case ROUND_ROBIN -> createRoundRobinSchedulingMode(tag, dirtyCallback);
        };
    }

    private static RoundRobinExporterSchedulingMode createRoundRobinSchedulingMode(@Nullable final CompoundTag tag,
                                                                                   final Runnable dirtyCallback) {
        final int index = tag != null ? tag.getInt(TAG_ROUND_ROBIN_INDEX) : 0;
        return new RoundRobinExporterSchedulingMode(new RoundRobinState(
            dirtyCallback,
            index
        ));
    }

    public void writeToTag(final CompoundTag tag, final ExporterSchedulingMode schedulingMode) {
        if (schedulingMode instanceof RoundRobinExporterSchedulingMode roundRobin) {
            tag.putInt(TAG_ROUND_ROBIN_INDEX, roundRobin.getIndex());
        }
    }
}
