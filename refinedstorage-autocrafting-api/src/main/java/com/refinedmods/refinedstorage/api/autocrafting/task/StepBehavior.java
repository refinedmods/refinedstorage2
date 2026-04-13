package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface StepBehavior {
    StepBehavior DEFAULT = new StepBehavior() {
    };

    default boolean canStep(final Pattern pattern) {
        return true;
    }

    default int getSteps(final Pattern pattern) {
        return 1;
    }
}
