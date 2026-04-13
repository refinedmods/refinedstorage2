package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SchedulingModeContainer {
    private static final String TAG_SCHEDULING_MODE = "sm";

    private final Consumer<SchedulingMode> listener;
    private final Runnable changeListener;

    private SchedulingModeType type;
    private SchedulingMode schedulingMode;

    public SchedulingModeContainer(final Consumer<SchedulingMode> listener,
                                   final Runnable changeListener) {
        this.listener = listener;
        this.changeListener = changeListener;
        this.type = SchedulingModeType.DEFAULT;
        this.schedulingMode = type.createSchedulingMode(
            null,
            tasks -> Collections.shuffle(tasks, new Random()),
            changeListener
        );
        notifyListeners(false);
    }

    public SchedulingModeType getType() {
        return type;
    }

    public void setType(final SchedulingModeType type) {
        setType(null, type, true);
    }

    private void setType(@Nullable final ValueInput input,
                         final SchedulingModeType newType,
                         final boolean changed) {
        this.type = newType;
        this.schedulingMode = newType.createSchedulingMode(
            input,
            tasks -> Collections.shuffle(tasks, new Random()),
            changeListener
        );
        notifyListeners(changed);
    }

    private void notifyListeners(final boolean changed) {
        listener.accept(schedulingMode);
        if (changed) {
            changeListener.run();
        }
    }

    public void read(final ValueInput input) {
        input.getInt(TAG_SCHEDULING_MODE).map(SchedulingModeType::getById)
            .ifPresent(t -> setType(input, t, false));
    }

    public void store(final ValueOutput output) {
        output.putInt(TAG_SCHEDULING_MODE, type.getId());
        type.store(output, schedulingMode);
    }

    public void execute(final List<? extends SchedulingMode.ScheduledTask> tasks) {
        schedulingMode.execute(tasks);
    }
}
