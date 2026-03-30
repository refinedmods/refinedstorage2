package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;

import java.util.List;

record LpStepPlan(List<LpExecutionPlanStep> steps, boolean hasRecipeCycles) {
    LpStepPlan {
        steps = List.copyOf(steps);
    }
}
